package com.darkyen.limas

import java.util.ArrayList

/**

 */
class ErrorContext(val fileName:String) {

    private val messages = ArrayList<Message>()

    fun log(message: Message) {
        messages.add(message)
    }

    fun error(location: Int, message: String) {
        messages.add(Message(Level.ERROR, location, message))
    }

    fun warn(location: Int, message: String) {
        messages.add(Message(Level.WARNING, location, message))
    }

    fun info(location: Int, message: String) {
        messages.add(Message(Level.INFO, location, message))
    }

    fun debug(location: Int, message: String) {
        messages.add(Message(Level.DEBUG, location, message))
    }

    fun mark() = messages.size

    fun rollback(mark:Int) {
        while (messages.size > mark) {
            messages.removeAt(messages.size - 1)
        }
    }

    fun hasErrors():Boolean {
        return messages.find { it.level == Level.ERROR } != null
    }

    class Message(val level: Level, val location: Int, val message: String) : Comparable<Message> {

        override fun compareTo(other: Message): Int {
            if (level != other.level) {
                return level.ordinal - other.level.ordinal
            } else {
                return location - other.location
            }
        }
    }

    enum class Level {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    fun printMessages(source:CharSequence, level:Level) {
        println("\n$fileName: ${messages.size} messages(s)")
        var warnings = 0
        var errors = 0
        for (message in messages) {
            if (message.level == Level.WARNING) {
                warnings++
            } else if (message.level == Level.ERROR) {
                errors++
            }

            if (message.level.ordinal >= level.ordinal) {
                println("%s at %d:%d".format(message.level, source.lineOfIndex(message.location), source.columnOfIndex(message.location)))
                println(message.message)
                println(source.previewOfLine(message.location))
                println()
            }
        }
        println("$warnings warnings, $errors errors")
    }
}
