package com.darkyen.limas

import com.darkyen.limas.LimaToken.*
import com.darkyen.limas.Node.*
import com.darkyen.limas.Node.Definition.*
import java.util.*

/**
 * Notes:
 * | = pin
 */
class AssemblyParser(text:CharSequence, errorContext: ErrorContext) : TokenParser(text, errorContext){

    /**
     * Scope :=
     *      Memory Definition
     *      Register Definition
     *      Instruction
     */
    fun parseScopeBody(scope: Scope) {
        while (true) {
            if (eof() || peek() == SCOPE_END) {
                break
            }

            // Parse MemoryDefinition
            val mem = parseMemoryDefinition(scope)
            if (mem != null) {
                scope.consume(mem)
                continue
            }

            // Parse RegisterDefinition
            //TODO

            // Parse Instruction
            val instruction = parseInstruction(scope)
            if (instruction != null) {
                scope.consume(instruction)
                continue
            }

            // Parse label
            val label = parseLabel(scope)
            if (label != null) {
                scope.consume(label)
                continue
            }

            // Parse Scope
            val nestedScope = parseScope(scope)
            if (nestedScope != null) {
                scope.consume(nestedScope)
                continue
            }


            // Unknown
            error("Invalid token ${next()} - ${tokenText()}")
        }
        // Done
    }

    fun parseLabel(parent: Node<*>):Label? {
        // "-"<identifier>
        if (match(LABEL_PREFIX)) {
            val labelName = parseIdentifierString()
            if (labelName == null) {
                error("Expected label name, got ${peek()}")
                return null
            }
            return Label(labelName).init(parent, tokenBegin(), tokenEnd())
        }
        return null
    }

    fun parseScope(parent: Node<*>):Scope? {
        // Scope: [<identifier>[":"<group identifier>]] "{" <scope content> "}"

        // Try match label group
        val scopeMark = mark()

        val scopeIdentifier:String? = parseIdentifierString()

        var groupIdentifier:String? = null
        if (scopeIdentifier != null && match(GROUP_SEPARATOR)) {
            groupIdentifier = parseIdentifierString()
            if (groupIdentifier == null) {
                error("Group identifier expected")
            }
        }
        var address:Long = -1
        if (match(ADDRESS_SPECIFIER)) {
            val a = parseIntegerLiteralLong()
            if (a == null) {
                error("Address specifier expected")
            } else {
                address = a
            }
        }

        if (match(SCOPE_BEGIN)) {
            val newScope = Scope(scopeIdentifier, groupIdentifier, address)
            val scopeBegin = tokenBegin()
            parseScopeBody(newScope)
            match(SCOPE_END, "Non-implicit scope must be closed")
            newScope.init(parent, scopeBegin, tokenEnd())

            return newScope
        } else {
            scopeMark.rollback()
            return null
        }
    }

    fun parseInstruction(parent: Node<*>):Instruction? {
        val instructionMark = mark()
        if (match(IDENTIFIER)) {
            val instructionBegin = tokenBegin()
            val mnemonic = tokenText()
            val argumentMark = mark()

            var matchedAny = false
            for (instruction in instructions) {
                if (!instruction.mnemonic.equals(mnemonic, true)) {
                    continue
                }
                matchedAny = true

                argumentMark.rollback()

                var wrong = false

                val inst = Instruction(instruction)
                for (asmPart in instruction.assemblyParts) {
                    when (asmPart) {
                        is Part.Immediate -> {
                            // Parse number or memory tag
                            val literal = parseIntegerLiteral(inst)
                            if (literal != null) {
                                inst.children.add(literal)
                            } else {
                                val tag = parseIdentifier(inst)
                                if (tag != null) {
                                    inst.children.add(tag)
                                } else {
                                    wrong = true
                                    error("Expected literal or memory tag, got ${peek()}")
                                }
                            }
                        }
                        is Part.Register -> {
                            // Parse register literal or register tag
                            val literal = parseRegisterLiteral(inst)
                            if (literal != null) {
                                inst.children.add(literal)
                            } else {
                                val tag = parseIdentifier(inst)
                                if (tag != null) {
                                    inst.children.add(tag)
                                } else {
                                    wrong = true
                                    error("Expected literal or register tag, got ${peek()}")
                                }
                            }
                        }
                        else ->
                            throw AssertionError("Unknown part $asmPart")
                    }
                }

                if (!wrong) {
                    return inst.init(parent, instructionBegin, tokenEnd())
                }
            }

            if (!matchedAny) {
                instructionMark.rollback()
            }
        }
        return null
    }

    /**
     * "def" <identifier> ["[" <array size> "]"] ["@" <address>] [initial value]
     */
    fun parseMemoryDefinition(parent:Node<*>):MemoryDefinition? {
        if (!match(DEFINE_MEMORY)) return null
        val definitionBegin = tokenBegin()

        val name = parseIdentifierString()
        if (name == null) {
            error("Expected memory definition identifier")
        }

        var byteSize:Long = 1
        if (match(ARRAY_BEGIN)) {
            val arraySize = parseIntegerLiteralLong()
            if (arraySize != null) {
                byteSize = arraySize
            } else {
                error("Expected array size literal after '['")
            }
            match(ARRAY_END, "Expected ']'")
        }

        if (byteSize <= 0) {
            error("Illegal array size $byteSize")
        }

        var address:Long = -1
        if (match(ADDRESS_SPECIFIER)) {
            val a = parseIntegerLiteralLong()
            if (a == null) {
                error("Expected address literal after '@'")
            } else {
                address = a
            }
        }

        val initialValues = ArrayList<Long>()
        while (true) {
            val i = parseIntegerLiteralLong()
            if (i != null) {
                initialValues.add(i)
            } else {
                break
            }
        }

        if (initialValues.size > byteSize) {
            error("Too much initial values (${initialValues.size}), needed max $byteSize")
        }

        return MemoryDefinition(name ?: "<null>", byteSize, address).apply {
            this.initialValues.addAll(initialValues)
            init(parent, definitionBegin, tokenEnd())
        }
    }

    fun parseIntegerLiteralLong():Long? {
        val base:Int
        when (peek()) {
            BINARY_LITERAL -> base = 2
            OCTAL_LITERAL -> base = 8
            HEXADECIMAL_LITERAL -> base = 16
            DECIMAL_LITERAL -> base = 10
            else ->
                    return null
        }

        next()
        var text = tokenText()
        if (base != 10 || (base == 10 && text.last() == 'd' || text.last() == 'D')) {
            text = text.substring(0, text.length-1)
        }

        return java.lang.Long.parseLong(text, base)
    }

    fun parseIntegerLiteral(parent:Node<*>):IntegerLiteral? {
        val number = parseIntegerLiteralLong() ?: return null
        return IntegerLiteral(number).init(parent, tokenBegin(), tokenEnd())
    }

    fun parseIdentifierString():String? {
        if (peek() == IDENTIFIER) {
            next()
            return tokenText()
        } else {
            return null
        }
    }

    fun parseIdentifier(parent:Node<*>):Identifier? {
        val name = parseIdentifierString() ?: return null
        return Identifier(name).init(parent, tokenBegin(), tokenEnd())
    }

    fun parseRegisterLiteral(parent:Node<*>):RegisterLiteral? {
        if (match(REGISTER_LITERAL)) {
            return RegisterLiteral((tokenText()[1] - '0').toByte()).init(parent, tokenBegin(), tokenEnd())
        } else return null
    }
}