package com.darkyen.limas

import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*

/**
 *
 */

val VERSION = "0.1"

fun main(args: Array<String>) {
    val options = HashMap<String, String>()
    val inputFiles = ArrayList<String>()

    var parsingOptions = true
    var optionName:String? = null
    for (arg in args) {
        if (parsingOptions) {
            if (arg == "--") {
                parsingOptions = false
                if (optionName != null) {
                    options[optionName] = ""
                    optionName = null
                }
            }

            if (arg.startsWith("-")) {
                if (optionName != null) {
                    options[optionName] = ""
                }
                optionName = arg.drop(1)
            } else {
                if (optionName == null) {
                    parsingOptions = false
                    inputFiles.add(arg)
                } else {
                    options[optionName] = arg
                    optionName = null
                }
            }
        } else {
            inputFiles.add(arg)
        }
    }

    if (options["version"] != null) {
        println("Limen Alpha Assembler $VERSION")
        println(" Copyright (c) 2017 Jan Polák")
        println(" Under MIT License")
        return
    }

    if (options["help"] != null || options["h"] != null || options["?"] != null) {
        println("limas [options] -- [input files]")
        println("Options:")
        println(" -out <directory>   set output directory, or '' for stdout, default is working directory")
        println(" -format <format>   set output format, allowed formats are:")
        println("                        bin - binary format, machine code without comments, undefined areas zeroed")
        println("                        ascii-bin - ascii binary, one address/line, with content comment separated by \\t, undefined lines empty")
        println(" -lenient           be lenient and don't halt compilation on ignorable errors")
        println(" -verbose           be verbose")
        println(" -log <level>       change log level")
        println(" -help              print this help")
        println(" -version           print version of this assembly compiler")
        println("Input files: paths of input files in .lima format.\n" +
                "If no input file is present, it is taken from stdin.")
        return
    }

    val verbose = options.containsKey("verbose")

    val output:File?
    if (options["out"] == null) {
        output = File(".").canonicalFile
    } else if (options["out"] == "-") {
        output = null
    } else {
        output = File(options["out"]).canonicalFile
        output.mkdirs()
        if (verbose) {
            println("Outputting to "+output.path)
        }
    }

    val inputStrings = ArrayList<String>()
    val inputStringsSources = ArrayList<String>()

    if (inputFiles.isEmpty()) {
        InputStreamReader(System.`in`).use { reader ->
            inputStrings.add(reader.readText())
            inputStringsSources.add("<stdin>")
        }
    } else {
        for (fileName in inputFiles) {
            val file = File(fileName)
            if (file.isFile) {
                FileReader(file).use { reader ->
                    inputStrings.add(reader.readText())
                    inputStringsSources.add(file.name)
                }
            } else {
                System.err.println("${file.canonicalPath} is not a file")
            }
        }
    }

    val lenient = options.containsKey("lenient")

    val logLevel:ErrorContext.Level
    if (!options.containsKey("log")) {
        logLevel = ErrorContext.Level.INFO
    } else {
        val level = ErrorContext.Level.values().find { it.name.equals(options["log"], true) }
        if (level == null) {
            System.err.println("Invalid log level "+options["log"])
            return
        }
        logLevel = level
    }

    val format:ResultFormat
    if (!options.containsKey("format")) {
        format = ResultFormat.ASCII_BINARY
    } else {
        val requested = ResultFormat.formatNamed(options["format"]!!)
        if (requested != null) {
            format = requested
            if (verbose) println("Output format set to "+requested.cliName)
        } else {
            System.err.println("Invalid format name "+options["format"])
            return
        }
    }

    for ((source, sourceFile) in inputStrings.zip(inputStringsSources)) {
        val errorContext = ErrorContext(sourceFile)

        val scope = Node.Scope(null)
        AssemblyParser(source, errorContext).parseScopeBody(scope)

        if (errorContext.hasErrors()) {
            errorContext.printMessages(source, logLevel)
            continue
        }

        val resolved = AssemblyCompiler.resolveAndCheck(scope, errorContext, lenient)

        if (resolved == null || errorContext.hasErrors()) {
            errorContext.printMessages(source, logLevel)
            continue
        }

        errorContext.printMessages(source, logLevel)

        val out:OutputStream

        if (output == null) {
            out = System.out
        } else {
            val file = File(output, sourceFile.replaceAfterLast('.', format.fileSuffix))
            out = FileOutputStream(file, false)
        }

        format.output(scope, resolved, out)

        if (output != null) out.close()
    }
}

enum class ResultFormat(val cliName:String, val fileSuffix:String) {
    BINARY("bin", "bin") {
        override fun output(program: Node.Scope, resolutionResult: AssemblyCompiler.ResolutionResult, to: OutputStream) {
            val machineCode = ByteArray((resolutionResult.allocatedWords() * 2).toInt())

            AssemblyCompiler.collectMemoryElements(program, object : AssemblyCompiler.BinaryGeneratingAssemblyCollector() {
                override fun collect(address: Long, value: Long, node: Node) {
                    machineCode[address.toInt() * 2] = ((value ushr 8) and 0xFF).toByte()
                    machineCode[address.toInt() * 2 + 1] = (value and 0xFF).toByte()
                }
            })

            to.write(machineCode)
        }
    },
    ASCII_BINARY("ascii-bin", "txt") {
        override fun output(program: Node.Scope, resolutionResult: AssemblyCompiler.ResolutionResult, to: OutputStream) {
            val lines = arrayOfNulls<CharSequence>(resolutionResult.allocatedWords().toInt())

            AssemblyCompiler.collectMemoryElements(program, object : AssemblyCompiler.BinaryGeneratingAssemblyCollector() {
                override fun collect(address: Long, value: Long, node: Node) {
                    val sb = StringBuilder()

                    for (i in IntProgression.fromClosedRange(15, 0, -1)) {
                        if ((value and (1L shl i)) == 0L) {
                            sb.append('0')
                        } else {
                            sb.append('1')
                        }
                    }

                    if (node is Node.MemoryMapped && node.address == address) {
                        sb.append('\t')
                        sb.append(node)
                    }

                    lines[address.toInt()] = sb
                }
            })

            val printer = OutputStreamWriter(to, StandardCharsets.UTF_8)
            for (line in lines) {
                if (line != null) {
                    printer.append(line)
                }
                printer.append('\n')
            }
            printer.flush()
        }
    };

    abstract fun output(program:Node.Scope, resolutionResult: AssemblyCompiler.ResolutionResult, to:OutputStream)

    companion object {
        fun formatNamed(name:String):ResultFormat? {
            for (f in ResultFormat.values()) {
                if (f.cliName.equals(name, true)) {
                    return f
                }
            }
            return null
        }
    }
}