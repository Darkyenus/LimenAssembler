package com.darkyen.limas

import java.util.*
import com.darkyen.limas.Node.*
import com.darkyen.limas.Node.Definition.*

/**
 *
 */

object AssemblyCompiler {

    class ResolutionResult(val allocations:List<Allocation>) {
        fun allocatedWords():Long {
            var highest:Long = 0
            for ((start, length) in allocations) {
                val high = start + length
                if (high > highest) {
                    highest = high
                }
            }
            return highest
        }
    }

    fun resolveAndCheck(rootScope: Scope, errorContext: ErrorContext, continueOnSoftErrors:Boolean):ResolutionResult? {
        val allocations = ArrayList<Allocation>()

        fun allocate(start: Long, length: Long, node: Node) {
            allocations.add(Allocation(start, length, node))
        }

        fun overlaps(start: Long, length: Long, group:String?):Allocation? {
            for (alloc in allocations) {
                if (alloc.overlaps(start, length)) {
                    if (group != null){
                        val node = alloc.node
                        if (node is Scope && group == node.group) {
                            //In same group, all is ok
                            continue
                        }
                    }

                    return alloc
                }
            }
            return null
        }

        var success = true

        // Check for invalid constructs
        traverse(rootScope) { node ->
            // Check for scopes with group and instructions
            if (node is Scope && node.group != null) {
                for (member in node.members) {
                    if (member !is Definition.MemoryDefinition) {
                        errorContext.error(member.begin, "Scope with group may contain only memory definitions")
                        success = false
                    }
                }
            }

            // Check that memory definitions in group scopes don't have default values
            if (node is MemoryDefinition) {
                val parentScope = node.parent as Scope
                if (parentScope.group != null && node.initialValues.size > 0) {
                    errorContext.error(node.begin, "Memory definitions in group scopes can't have default values")
                    if (!continueOnSoftErrors) success = false
                }
            }

            // Check for scopes with explicit address, with instructions, not ending with jump
            if (node is Scope && node.explicitAddress != -1L) {
                var lastInstruction:Instruction? = null
                for (member in node.members) {
                    if (member is Instruction) {
                        lastInstruction = member
                    }
                }
                if (lastInstruction != null && !lastInstruction.type.canBeReturn) {
                    errorContext.warn(node.end, "Explicitly placed scope is missing return-like end instruction, this may be a bug")
                }
            }

            // Check for invalid register definitions
            if (node is RegisterDefinition) {
                traverseUp(node) {
                    if (it is RegisterUndefinition && it.name == node.name) {
                        // Undefined,
                        return@traverseUp false
                    }
                    if (it is RegisterDefinition && it.register == node.register) {
                        errorContext.warn(node.begin, "Register redefinition...")
                        errorContext.warn(it.begin, "...previously defined here")
                    }
                    return@traverseUp true
                }
            }

            // Check for invalid register un-definitions
            if (node is RegisterUndefinition) {
                traverseUp(node) {
                    if (it is RegisterUndefinition && it.name == node.name) {
                        errorContext.warn(node.begin, "Register double undefinition...")
                        errorContext.warn(it.begin, "...previously undefined here")
                    }
                    return@traverseUp true
                }
            }

            return@traverse true
        }

        // Fill in explicitly placed
        traverse(rootScope) { node ->
            if (node is MemoryMapped && node.explicitAddress != -1L) {
                val start = node.explicitAddress
                val length = node.wordSize(false)

                //Find all overlapping blocks and check if they don't pose a problem
                var overlapping:Allocation? = null
                val myGroup = if (node is Scope) node.group else null
                for (alloc in allocations) {
                    if (alloc.overlaps(start, length)) {
                        val aNode = alloc.node
                        if (myGroup != null && aNode is Scope && aNode.group == myGroup) {
                            // Fine
                        } else {
                            overlapping = alloc
                            break
                        }
                    }
                }

                if (overlapping == null) {
                    node.address = start
                    allocate(start, length, node)
                    errorContext.debug(node.begin, "Allocated explicitly to $start")
                } else {
                    errorContext.error(node.begin, "Can't allocate space for $node, collides with $overlapping")
                    success = false
                }
            }
            return@traverse true
        }

        if (!success) return null

        // Find unresolved nodes and resolve them (this should be
        traverse(rootScope) { node ->
            if (node !is MemoryMapped || node.address != -1L) return@traverse true
            val length = node.wordSize(true)
            val group = if (node is Scope) node.group else null
            var possibleAddress = 0L
            var allocationIndex = -1

            while (true) {
                if (overlaps(possibleAddress, length, group) != null) {
                    // No luck
                    allocationIndex++
                    val allocation = allocations[allocationIndex]
                    possibleAddress = allocation.start + allocation.length
                } else {
                    // Looks good
                    node.address = possibleAddress
                    allocate(possibleAddress, length, node)
                    errorContext.debug(node.begin, "Allocated implicitly to $possibleAddress")
                    break
                }
            }
            return@traverse true
        }

        if (!success) return null

        // Resolve instructions
        traverse(rootScope) { node ->
            if (node is Instruction) {
                args@for (arg in node.args) {
                    when (arg) {
                        is ArgRegister -> {
                            if (arg.reg.isResolved()) continue@args

                            val handled = !traverseUp(arg) { regDef ->
                                if (regDef is RegisterDefinition && regDef.name == arg.reg.identifier) {
                                    arg.reg.resolution = regDef.register
                                    errorContext.debug(arg.begin, "Resolved register '${regDef.name}' to '${regDef.register}'")
                                    return@traverseUp false
                                } else if (regDef is RegisterUndefinition && regDef.name == arg.reg.identifier) {
                                    errorContext.error(arg.begin, "Can't resolve register because...")
                                    errorContext.error(regDef.begin, "...it is undefined here")
                                    success = false
                                    return@traverseUp false
                                } else return@traverseUp true
                            }

                            if (!handled) {
                                errorContext.error(arg.begin, "Can't resolve register identifier '${arg.reg.identifier}'")
                                success = false
                            }
                        }
                        is ArgImmediate -> {
                            if (arg.imm.isResolved()) continue@args

                            errorContext.debug(arg.begin, "Resolving immediate '${arg.imm.identifier}'...")
                            val handled = !traverseUpForward(arg) { memDef ->
                                errorContext.debug(memDef.begin, "   ...trying $memDef")
                                if (memDef is MemoryDefinition && memDef.name == arg.imm.identifier) {
                                    arg.imm.resolution = memDef.address
                                    errorContext.debug(arg.begin, "Resolved immediate to memory '${memDef.address}'")
                                    return@traverseUpForward false
                                } else if (memDef is Label && memDef.name == arg.imm.identifier) {
                                    arg.imm.resolution = memDef.address
                                    errorContext.debug(arg.begin, "Resolved immediate to label '${memDef.address}'")
                                    return@traverseUpForward false
                                } else if (memDef is Scope && memDef.name == arg.imm.identifier) {
                                    if (memDef.group != null) {
                                        errorContext.warn(arg.begin, "Immediate referencing group scope is probably wrong, as jumping there is probably a bug and value at it's address is undefined")
                                    }
                                    arg.imm.resolution = memDef.address
                                    errorContext.debug(arg.begin, "Resolved immediate to scope '${memDef.address}'")
                                    return@traverseUpForward false
                                } else if (memDef is Scope && memDef.group != null) {
                                    for (def in memDef.members) {
                                        if (def is MemoryDefinition && def.name == arg.imm.identifier) {
                                            arg.imm.resolution = def.address
                                            errorContext.debug(arg.begin, "Resolved immediate to '${def.address}'")
                                            return@traverseUpForward false
                                        }
                                    }
                                }
                                return@traverseUpForward true
                            }

                            if (!handled) {
                                errorContext.error(arg.begin, "Can't resolve immediate identifier ${arg.imm.identifier}")
                                success = false
                            }
                        }
                    }
                }
            }

            return@traverse true
        }

        if (!success) return null

        // Translate relative jumps
        traverse(rootScope) { node ->
            if (node !is ArgImmediate) return@traverse true
            val instructionPart = node.instructionPart() as Part.Immediate
            if(!instructionPart.relativeAddress) return@traverse true

            val myAddress = (node.parent as Instruction).address
            val targetAddress = node.imm.resolution!!
            val addressOffset = targetAddress - myAddress

            node.imm.resolution = addressOffset
            return@traverse true
        }

        // Check that no instruction has too big immediate value
        traverse(rootScope) { node ->
            if (node !is ArgImmediate) return@traverse true
            val instructionPart = node.instructionPart()
            val signed = (instructionPart as Part.Immediate).signed
            val bitWidth = instructionPart.width
            val value:Long = node.imm.resolution!!

            val masked = value and ((1L shl bitWidth) - 1L)
            val truncated:Long
            if (signed) {
                val shift = java.lang.Long.SIZE - bitWidth
                val shiftedValue = (masked shl shift) shr shift
                truncated = shiftedValue
            } else {
                truncated = masked
            }

            if (value != truncated) {
                if (!continueOnSoftErrors) success = false
                errorContext.error(node.begin, "${if (signed) "Signed" else "Unsigned"} value '$value' ('0x${java.lang.Long.toHexString(value)}') is too large for $bitWidth bit wide instruction")
            }
            return@traverse true
        }

        if (!success) return null

        return ResolutionResult(allocations)
    }

    data class Allocation(val start:Long, val length:Long, val node: Node) : Comparable<Allocation> {

        override fun compareTo(other: Allocation): Int {
            if (start < other.start) {
                return -1
            } else if (start > other.start) {
                return 1
            } else {
                if (length < other.length) {
                    return -1
                } else if (length > other.length) {
                    return 1
                } else {
                    return 0
                }
            }
        }

        fun overlaps(start: Long, length: Long):Boolean {
            return start < this.start + this.length && start + length > this.start
        }

        override fun toString(): String {
            return "%s\n@ 0x%2\$X (%2\$d) - 0x%3\$X (%3\$d)".format(node, start, start + length)
        }
    }

    fun collectMemoryElements(rootScope: Scope, collector:AssemblyCollector) {
        traverse(rootScope) { node ->
            when (node) {
                is Instruction ->
                        collector.collect(node.address, node)
                is MemoryDefinition -> {
                    if ((node.parent as Scope).group == null) {
                        collector.collect(node.address, node)
                    }
                }

            }
            return@traverse true
        }
    }

    interface AssemblyCollector {
        fun collect(address:Long, instruction:Instruction)
        fun collect(firstAddress:Long, memoryDefinition:MemoryDefinition)
    }

    abstract class BinaryGeneratingAssemblyCollector : AssemblyCollector {
        override fun collect(address: Long, instruction: Instruction) {
            collect(address, instruction.machineCode(), instruction)
        }

        override fun collect(firstAddress: Long, memoryDefinition: MemoryDefinition) {
            var address = firstAddress
            val initialValues = memoryDefinition.initialValues
            for (initialValue in initialValues) {
                collect(address, initialValue, memoryDefinition)
                address++
            }
        }

        abstract fun collect(address: Long, value:Long, node:Node)
    }
}