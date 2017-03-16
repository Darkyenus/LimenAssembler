package com.darkyen.limas

import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
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
                if (optionName == null) {
                    optionName = arg.drop(1)
                } else {
                    options[optionName] = ""
                }
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
        println(" -help\tprint this help")
        println(" -version\tprint version of this assembly compiler")
        println("Input files: paths of input files in .lima format.\n" +
                "If no input file is present, it is taken from stdin.")
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

    for ((source, sourceFile) in inputStrings.zip(inputStringsSources)) {
        val errorContext = ErrorContext(sourceFile)

        val scope = Node.Scope(null)
        AssemblyParser(source, errorContext).parseScopeBody(scope)

        if (errorContext.hasErrors()) {
            errorContext.printMessages(source)
            continue
        }

        println("TODO: Compile and output")
    }
}