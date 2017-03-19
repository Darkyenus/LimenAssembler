package com.darkyen.limas

import com.darkyen.limas.Node.*
import java.util.*

/**
 *
 */

sealed class Node {

    var parent:Node? = null
        protected set
    var begin:Int = 0
        protected set
    var end:Int = 0
        protected set

    class Scope(val name:String?, val group:String? = null, override val explicitAddress:Long = -1) : Node(), MemoryMapped {
        override var address: Long = -1
            set(address) {
                field = address

                var a = address
                for (member in members) {
                    if (member is MemoryMapped && member.explicitAddress == -1L) {
                        member.address = a
                        a += member.wordSize(true)
                    }
                }
            }

        override fun wordSize(inScope: Boolean): Long {
            if (inScope && explicitAddress != -1L) return 0

            var sum = 0L
            for (member in members) {
                if (member is MemoryMapped) {
                    sum += member.wordSize(inScope)
                }
            }
            return sum
        }

        val members = mutableListOf<Node>()

        fun name():String {
            return this.name ?: "<anonymous>"
        }

        override fun toString(): String {
            return "${name()}${if (group != null) " :$group" else ""}${if (explicitAddress != -1L) " @$explicitAddress" else ""} { ${members.size} }${if (address != -1L) " at $address" else ""}"
        }
    }

    sealed class Definition(val name:String) : Node() {

        class MemoryDefinition(name:String, val wordSize:Long = 1, override val explicitAddress:Long = -1) : Definition(name), MemoryMapped {
            override var address: Long = -1

            override fun wordSize(inScope: Boolean) = if (inScope && explicitAddress != -1L) 0 else wordSize

            val initialValues = ArrayList<Long>()

            override fun toString(): String {
                return "def $name[$wordSize] ${if (explicitAddress != -1L) " @$explicitAddress" else ""} ${initialValues.joinToString(", ", prefix = "[", postfix = "]")}"
            }
        }

        class RegisterDefinition(name:String, val register:Register) : Definition(name) {
            override fun toString(): String {
                return "defr $name $register"
            }
        }

        class RegisterUndefinition(name:String) : Definition(name) {
            override fun toString(): String {
                return "undefr $name"
            }
        }
    }

    class Instruction(val type: InstructionType) : Node(), MemoryMapped {
        override var address: Long = -1

        override fun wordSize(inScope: Boolean): Long = 1

        val args = ArrayList<Node>()

        fun machineCode():Long {
            var result = 0L

            fun append(value: Long, width:Int) {
                val mask = (1L shl width) - 1
                result = (result shl width) or (value and mask)
            }

            type.codeParts.forEach { part ->
                when (part) {
                    is Part.Static -> {
                        append(part.value.toLong(), part.width)
                    }
                    is Part.Register -> {
                        append((args[part.order] as Node.ArgRegister).reg.resolution!!.code, part.width)
                    }
                    is Part.Immediate -> {
                        append((args[part.order] as Node.ArgImmediate).imm.resolution!!, part.width)
                    }
                }
            }
            return result
        }

        override fun toString(): String {
            return args.joinToString(" ", prefix = type.mnemonic+" ")
        }
    }

    abstract class Arg : Node() {
        fun instructionPart():Part {
            val instruction = parent as Instruction
            val index = instruction.args.indexOf(this)
            return instruction.type.assemblyParts[index]
        }
    }

    /**
     * @param imm Triple of identifier, word offset and byte part
     */
    class ArgImmediate(val imm: Resolvable<Triple<String, Long, BytePart>,Long>) : Arg() {

        override fun toString(): String {
            return imm.toString()
        }

        enum class BytePart(val keyword:String) {
            WORD(""),
            HIGH_BYTE("<"),
            LOW_BYTE(">");

            fun adjust(value:Long):Long {
                return when (this) {
                    WORD -> value
                    HIGH_BYTE -> (value shr 8) and 0xFF
                    LOW_BYTE -> value and 0xFF
                }
            }
        }
    }

    class ArgRegister(val reg: Resolvable<String, Register>) : Arg() {
        override fun toString(): String {
            return reg.toString()
        }
    }

    class Label(val name: String) : Node(), MemoryMapped {
        override var address: Long = -1

        override fun wordSize(inScope: Boolean): Long = 0

        override fun toString(): String {
            return "-$name${if (address != -1L) " at $address" else ""}"
        }
    }

    interface MemoryMapped {
        val explicitAddress:Long
            get() = -1
        var address:Long
        fun wordSize(inScope: Boolean): Long
    }

    companion object {
        fun <N:Node> N.init(parent: Node?, begin: Int, end: Int): N {
            this.parent = parent
            this.begin = begin
            this.end = end
            return this
        }
    }
}

class Resolvable<out From, To> constructor(val identifier:From?, var resolution:To?) {
    fun isResolved():Boolean = resolution != null

    override fun toString(): String {
        if (identifier == null) {
            return resolution.toString()
        } else if (resolution == null) {
            return identifier.toString()
        } else {
            return "$identifier ($resolution)"
        }
    }
}

enum class Register(val code:Long) {
    R0(0),
    R1(1),
    R2(2),
    R3(3),
    R4(4),
    R5(5),
    R6(6),
    R7(7),
    INVALID(-1)
}

/**
 * @param visitor callback, called with every node of node's subtree, including node itself. Return true to continue visiting, false to stop
 * @return True if whole tree traversed, false if cancelled by visitor
 */
fun traverse(node: Node, visitor:(Node)->Boolean):Boolean {
    if (!visitor(node)) return false
    if (node is Scope) {
        for (member in node.members) {
            if (!traverse(member, visitor)) return false
        }
    } else if (node is Instruction) {
        for (arg in node.args) {
            if (!traverse(arg, visitor)) return false
        }
    }
    return true
}

/**
 * @param visitor callback, called for every node before in block or before in parent block. Return true to continue, false to stop traversing
 */
fun traverseUp(node: Node, visitor: (Node) -> Boolean):Boolean {
    var current = node
    var parent = node.parent
    while (true) {
        if (parent == null) return true
        if (parent is Scope) {
            var index = parent.members.indexOf(current)
            if (index == -1) error("Invalid AST")
            while (index > 0) {
                index--
                if (!visitor(parent.members[index])) return false
            }
        }
        if(!visitor(parent)) return false
        current = parent
        parent = parent.parent
    }
}

/**
 * @param visitor callback, called for every node before in block or before OR after in parent block. Return true to continue, false to stop traversing
 */
inline fun traverseUpForward(node: Node, visitor: (Node) -> Boolean):Boolean {
    var current = node
    var parent = node.parent
    while (true) {
        if (parent == null) return true
        if (parent is Scope) {
            val myIndex = parent.members.indexOf(current)
            if (myIndex == -1) error("Invalid AST")

            var index = parent.members.size - 1
            while (index > 0) {
                index--
                if (index != myIndex) {
                    if (!visitor(parent.members[index])) return false
                }
            }
        }
        if(!visitor(parent)) return false
        current = parent
        parent = parent.parent
    }
}

/**
 * @param visitor callback, called for every node after in block and subsequent nested blocks. Return true to continue, false to stop traversing
 */
fun traverseDown(node: Node, visitor: (Node) -> Boolean):Boolean {
    var current = node
    var parent = node.parent
    while (parent !is Scope) {
        if (parent == null) {
            error("node $node is not in any scope")
        }
        current = parent
        parent = parent.parent
    }
    val parentScope:Scope = parent

    val index = parentScope.members.indexOf(current)
    if (index == -1) error("Invalid AST")
    val membersSize = parentScope.members.size

    for (i in (index + 1)..membersSize) {
        if(!traverse(parentScope.members[i], visitor)) return false
    }
    return true
}

private class InstructionBuilder(val mnemonic:String) {

    private val parts = mutableListOf<Part>()

    fun static(value: Int, width:Int): InstructionBuilder {
        parts.add(Part.Static(value, width))
        return this
    }

    fun register(order:Int): InstructionBuilder {
        parts.add(Part.Register(order))
        return this
    }

    fun immediate(order: Int, width:Int, signed:Boolean = false, relativeAddress: Boolean = false): InstructionBuilder {
        parts.add(Part.Immediate(order, width, signed, relativeAddress))
        return this
    }

    fun build(canBeReturn: Boolean = false):InstructionType {
        return InstructionType(
                mnemonic,
                parts.toTypedArray(),
                parts.filter { it.order >= 0 }.sortedBy(Part::order).toTypedArray(),
                canBeReturn)
    }
}

sealed class Part {
    open val order:Int
        get() = -1
    abstract val width:Int

    class Static(val value:Int, override val width:Int) : Part()

    class Register(override val order:Int) : Part() {
        override val width: Int
            get() = 3
    }

    class Immediate(override val order:Int, override val width:Int, val signed:Boolean, val relativeAddress:Boolean) : Part()
}

class InstructionType(
        val mnemonic: String,
        val codeParts:Array<Part>,
        val assemblyParts:Array<Part>,
        val canBeReturn:Boolean)

val instructions:Array<InstructionType> = arrayOf(
        // 5.2 Immediate arithmetic
        InstructionBuilder("SLU").static(0b000, 3).static(0b00, 2).immediate(2, 5, signed = true).register(1).register(0).build(),
        InstructionBuilder("SL").static(0b000, 3).static(0b01, 2).immediate(2, 5, signed = true).register(1).register(0).build(),
        // X
        InstructionBuilder("ADD").static(0b000, 3).static(0b11, 2).immediate(2, 5, signed = true).register(1).register(0).build(),

        // 5.3 Transfer with immediate displacement
        InstructionBuilder("LL").static(0b001, 3).static(0b00, 2).immediate(2, 5, signed = true).register(1).register(0).build(),
        InstructionBuilder("LD").static(0b001, 3).static(0b01, 2).immediate(2, 5, signed = true).register(1).register(0).build(),
        InstructionBuilder("SC").static(0b001, 3).static(0b10, 2).immediate(2, 5, signed = true).register(1).register(0).build(),
        InstructionBuilder("ST").static(0b001, 3).static(0b11, 2).immediate(2, 5, signed = true).register(1).register(0).build(),

        // 5.4 Logic with immediate value
        InstructionBuilder("OR").static(0b010, 3).static(0b000, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("ORN").static(0b010, 3).static(0b001, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("AND").static(0b010, 3).static(0b010, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("ANDN").static(0b010, 3).static(0b011, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("XOR").static(0b010, 3).static(0b100, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("SLL").static(0b010, 3).static(0b101, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("SRL").static(0b010, 3).static(0b110, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("SRA").static(0b010, 3).static(0b111, 3).immediate(2, 4).register(1).register(0).build(),

        // 5.5 Arithmetic, logic and control with registers
        InstructionBuilder("OR").static(0b011, 3).static(0b0000, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("ORN").static(0b011, 3).static(0b0001, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("AND").static(0b011, 3).static(0b0010, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("ANDN").static(0b011, 3).static(0b0011, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("XOR").static(0b011, 3).static(0b0100, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SLL").static(0b011, 3).static(0b0101, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SRL").static(0b011, 3).static(0b0110, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SRA").static(0b011, 3).static(0b0111, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SLU").static(0b011, 3).static(0b1000, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SL").static(0b011, 3).static(0b1001, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SUB").static(0b011, 3).static(0b1010, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("ADD").static(0b011, 3).static(0b1011, 4).register(2).register(1).register(0).build(),
        // X
        // X
        InstructionBuilder("RTC").static(0b011, 3).static(0b1110, 4).register(0).immediate(1, 3).static(0b000, 3).build(),
        InstructionBuilder("CTR").static(0b011, 3).static(0b1111, 4).static(0b000, 3).immediate(1, 3).register(0).build(),

        // 5.6 Load immediate value
        InstructionBuilder("LI").static(0b100, 3).immediate(1, 8).static(0b00, 2).register(0).build(),
        InstructionBuilder("LIS").static(0b100, 3).immediate(1, 8).static(0b01, 2).register(0).build(),
        InstructionBuilder("LIL").static(0b100, 3).immediate(1, 8).static(0b10, 2).register(0).build(),
        InstructionBuilder("LIH").static(0b100, 3).immediate(1, 8).static(0b11, 2).register(0).build(),

        // 5.7 Conditional jump with immediate displacement
        InstructionBuilder("JNE").static(0b101, 3).immediate(1, 7, signed = true, relativeAddress = true).register(0).static(0b010, 3).build(),
        InstructionBuilder("JE").static(0b101, 3).immediate(1, 7, signed = true, relativeAddress = true).register(0).static(0b011, 3).build(),
        InstructionBuilder("JL").static(0b101, 3).immediate(1, 7, signed = true, relativeAddress = true).register(0).static(0b100, 3).build(),
        InstructionBuilder("JLE").static(0b101, 3).immediate(1, 7, signed = true, relativeAddress = true).register(0).static(0b101, 3).build(),
        InstructionBuilder("JG").static(0b101, 3).immediate(1, 7, signed = true, relativeAddress = true).register(0).static(0b110, 3).build(),
        InstructionBuilder("JGE").static(0b101, 3).immediate(1, 7, signed = true, relativeAddress = true).register(0).static(0b111, 3).build(),

        // 5.8 Unconditional jump with immediate displacement
        InstructionBuilder("JWL").static(0b110, 3).immediate(1, 10, signed = true, relativeAddress = true).register(0).build(canBeReturn = true),

        // 5.9 Unconditional jump with registers
        InstructionBuilder("JWL").static(0b111, 3).static(0, 7).register(1).register(0).build(canBeReturn = true)
)