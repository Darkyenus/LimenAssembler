package com.darkyen.limas

import java.util.*

/**
 *
 */

sealed class Node<Self : Node<Self>> {

    var parent:Node<*>? = null
        protected set
    var begin:Int = 0
        protected set
    var end:Int = 0
        protected set

    fun init(parent: Node<*>?, begin: Int, end: Int): Self {
        this.begin = begin
        this.end = end
        @Suppress("UNCHECKED_CAST")
        return this as Self
    }


    class Scope(val name:String?, val group:String? = null, val address:Long = -1) : Node<Scope>(), Consumer<Node<*>> {
        val members = mutableListOf<Node<*>>()

        fun name():String {
            return this.name ?: "<anonymous>"
        }

        override fun consume(o: Node<*>) {
            members.add(o)
        }
    }

    sealed class Definition<Self : Definition<Self>>(val name:String) : Node<Definition<Self>>() {
        class MemoryDefinition(name:String, val byteSize:Long = 1, val explicitAddress:Long = -1) : Definition<MemoryDefinition>(name) {
            val initialValues = ArrayList<Long>()
        }

        class RegisterDefinition(name:String, val register:Int) : Definition<RegisterDefinition>(name)
    }

    class Instruction(val type: InstructionType) : Node<Instruction>() {
        val children = ArrayList<Node<*>>()
    }

    class Identifier(val name: String) : Node<Identifier>()
    class IntegerLiteral(val value: Long) : Node<IntegerLiteral>()
    class RegisterLiteral(val reg: Byte) : Node<RegisterLiteral>()

    class Label(val name: String) : Node<Label>()
}

private class InstructionBuilder(val mnemonic:String) {

    private val parts = mutableListOf<Part>()

    fun literal(value: Int, width:Int): InstructionBuilder {
        parts.add(Part.Literal(value, width))
        return this
    }

    fun register(order:Int): InstructionBuilder {
        parts.add(Part.Register(order))
        return this
    }

    fun immediate(order: Int, width:Int): InstructionBuilder {
        parts.add(Part.Immediate(order, width))
        return this
    }

    fun build():InstructionType {
        return InstructionType(mnemonic, parts.toTypedArray(), parts.filter { it.order >= 0 }.sortedBy(Part::order).toTypedArray())
    }
}

sealed class Part {
    open val order:Int
        get() = -1
    abstract val width:Int

    class Literal(val value:Int, override val width:Int) : Part()

    class Register(override val order:Int) : Part() {
        override val width: Int
            get() = 3
    }

    class Immediate(override val order:Int, override val width:Int) : Part()
}

class InstructionType(val mnemonic: String, val codeParts:Array<Part>, val assemblyParts:Array<Part>)

val instructions:Array<InstructionType> = arrayOf(
        // 5.2 Immediate arithmetic
        InstructionBuilder("SLU").literal(0b000, 3).literal(0b00, 2).immediate(2, 5).register(1).register(0).build(),
        InstructionBuilder("SL").literal(0b000, 3).literal(0b01, 2).immediate(2, 5).register(1).register(0).build(),
        // X
        InstructionBuilder("ADD").literal(0b000, 3).literal(0b11, 2).immediate(2, 5).register(1).register(0).build(),

        // 5.3 Transfer with immediate displacement
        InstructionBuilder("LL").literal(0b001, 3).literal(0b00, 2).immediate(2, 5).register(1).register(0).build(),
        InstructionBuilder("LD").literal(0b001, 3).literal(0b01, 2).immediate(2, 5).register(1).register(0).build(),
        InstructionBuilder("SC").literal(0b001, 3).literal(0b10, 2).immediate(2, 5).register(1).register(0).build(),
        InstructionBuilder("ST").literal(0b001, 3).literal(0b11, 2).immediate(2, 5).register(1).register(0).build(),

        // 5.4 Logic with immediate value
        InstructionBuilder("OR").literal(0b010, 3).literal(0b000, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("ORN").literal(0b010, 3).literal(0b001, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("AND").literal(0b010, 3).literal(0b010, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("ANDN").literal(0b010, 3).literal(0b011, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("XOR").literal(0b010, 3).literal(0b100, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("SLL").literal(0b010, 3).literal(0b101, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("SRL").literal(0b010, 3).literal(0b110, 3).immediate(2, 4).register(1).register(0).build(),
        InstructionBuilder("SRA").literal(0b010, 3).literal(0b111, 3).immediate(2, 4).register(1).register(0).build(),

        // 5.5 Arithmetic, logic and control with registers
        InstructionBuilder("OR").literal(0b011, 3).literal(0b0000, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("ORN").literal(0b011, 3).literal(0b0001, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("AND").literal(0b011, 3).literal(0b0010, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("ANDN").literal(0b011, 3).literal(0b0011, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("XOR").literal(0b011, 3).literal(0b0100, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SLL").literal(0b011, 3).literal(0b0101, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SRL").literal(0b011, 3).literal(0b0110, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SRA").literal(0b011, 3).literal(0b0111, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SLU").literal(0b011, 3).literal(0b1000, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SL").literal(0b011, 3).literal(0b1001, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("SUB").literal(0b011, 3).literal(0b1010, 4).register(2).register(1).register(0).build(),
        InstructionBuilder("ADD").literal(0b011, 3).literal(0b1011, 4).register(2).register(1).register(0).build(),
        // X
        // X
        InstructionBuilder("RTC").literal(0b011, 3).literal(0b1110, 4).register(0).immediate(1, 3).literal(0b000, 3).build(),
        InstructionBuilder("CTR").literal(0b011, 3).literal(0b1111, 4).literal(0b000, 3).immediate(1, 3).register(0).build(),

        // 5.6 Load immediate value
        InstructionBuilder("LI").literal(0b100, 3).immediate(1, 8).literal(0b00, 2).register(0).build(),
        InstructionBuilder("LIS").literal(0b100, 3).immediate(1, 8).literal(0b01, 2).register(0).build(),
        InstructionBuilder("LIL").literal(0b100, 3).immediate(1, 8).literal(0b10, 2).register(0).build(),
        InstructionBuilder("LIH").literal(0b100, 3).immediate(1, 8).literal(0b11, 2).register(0).build(),

        // 5.7 Conditional jump with immediate displacement
        InstructionBuilder("JNE").literal(0b101, 3).immediate(1, 7).register(0).literal(0b010, 3).build(),
        InstructionBuilder("JE").literal(0b101, 3).immediate(1, 7).register(0).literal(0b011, 3).build(),
        InstructionBuilder("JL").literal(0b101, 3).immediate(1, 7).register(0).literal(0b100, 3).build(),
        InstructionBuilder("JLE").literal(0b101, 3).immediate(1, 7).register(0).literal(0b101, 3).build(),
        InstructionBuilder("JG").literal(0b101, 3).immediate(1, 7).register(0).literal(0b110, 3).build(),
        InstructionBuilder("JGE").literal(0b101, 3).immediate(1, 7).register(0).literal(0b111, 3).build(),

        // 5.8 Unconditional jump with immediate displacement
        InstructionBuilder("JWL").literal(0b110, 3).immediate(1, 10).register(0).build(),

        // 5.9 Unconditional jump with registers
        InstructionBuilder("JWL").literal(0b111, 3).literal(0, 7).register(1).register(0).build()
)

interface Consumer<in T> {
    fun consume(o:T)
}