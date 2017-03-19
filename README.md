# Limen Assembler

### How to run
- Make sure you have Java installed
- Obtain `LimenAssembler.jar` from releases or by compiling it yourself (IntelliJ IDEA recommended)
- From command line/terminal, call `java -jar LimenAssembler.jar <args>` (with the .jar in your working directory)
- Put `-help` in place of `<args>` to see which arguments are available

### How to write
While this assembly syntax is similar to other assemblies, there are some important differences.
Comments are C-like (that is `//...` for line comment and `/*...*/` for block comment).
Instructions are like in specification (see `./LimenAlphaResources/Limen Alpha Programmer's manual.pdf`),
but without commas between arguments. Whitespace is not significant.

#### Labels
This is a label: `-foobar`. In scope where it appears (and in any children scopes), this binds the address of next object
(instruction/memory definition) to the identifier `foobar`, which may then be used as an immediate argument to instructions.

#### Memory definitions
Memory definitions are statements, which allocate memory regions for storing data.
They look like this:
```
// Allocate here one word and initialize it with number 3
// Also binds the identifier "enginners_pi" to the address which was allocated
def engineers_pi 3

// Allocate one word at the address decimal 1000
def answer@1000 42

// Allocate here 5 words and fill them with numbers 1, 1, 2, 3 and 5
def fibb[5] 1 1 2 3 5

// Allocate 100 words at address 1001 hexadecimal and initialize them with zeroes
// Any number of default arguments, less or equal to the length may be used,
// missing values are implicitly set to zero
def big_blank[100]@1001h
```


#### Scopes
These can be used to create a hierarchy in code and to hide memory definitions from unrelated parts of the program.
Scopes can be named, which binds the address of the first object in scope to its identifier (useful when scopes are used like functions, or just for documentation).
Using syntax similar to memory definitions, scopes can be also explicitly placed in memory, which can be used to place specific instruction at a specific place in memory.
Scopes can be nested, but they don't overlap.

```
@24 {
	// Anonymous scope at address 24
}

// Scope called main, placed at address 5 with instructions to jump to address 5
main@5 {
	LI R4 main
	JWL R0 R4
}

other_main {
	// This implicitly placed scope also sees identifier main, because it is defined in parent scope
	LI R4 main
    JWL R0 R4
}

big_scope@100 {
	def big_variable[100]
}

// This will cause compile-time error, because this scope can't overlap with big_scope. Changing the address to point before or after big_scope would fix this problem.
small_scope@150 {
	def small_variable[1]
}
```

#### Register definitions
Memory definitions bind identifiers to addresses, register definitions bind identifiers to registers.
These statements don't have any representation in resulting machine code, but they can help with readability.

Registers literals are those identifiers R0, R1, R2, ..., R7.
Register identifiers are identifiers, which are always preceded with `$`, to prevent ambiguity.

Unlike memory definitions, register definitions can be undefined, but they are still scoped.

```
defr $target_address R6
defr $return_value R7

JWL $return_value $target_address

undefr $target_address
undefr $return_value
```

### Further development TODO's
- Allow to take only high/low byte of the identifier
- Allow to statically "index" defined word arrays
- Add constant definition
- Add more warnings