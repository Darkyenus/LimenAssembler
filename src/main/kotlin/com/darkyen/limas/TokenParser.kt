package com.darkyen.limas

import com.darkyen.limas.LimaToken.*
import java.util.*

/**
 *
 */
abstract class TokenParser(val text:CharSequence, val log: ErrorContext) {

    private val lexer = LimaFlexLexer(null)

    private val tokenBegins = ArrayList<Int>()
    private val tokenEnds = ArrayList<Int>()
    private val tokens = ArrayList<LimaToken>()
    var position = -1
        private set

    init {
        lexer.reset(text, 0, text.length, LimaFlexLexer.YYINITIAL)

        tokenCruncher@while (true) {
            val token: LimaToken = lexer.advance() ?: break
            val start = lexer.tokenStart
            val end = lexer.tokenEnd

            when (token) {
                WHITE_SPACE, BLOCK_COMMENT, LINE_COMMENT -> {
                    continue@tokenCruncher
                }
                UNKNOWN -> {
                        log.error(start, "Unknown token")
                        continue@tokenCruncher
                }
                else -> {
                    tokens.add(token)
                    tokenBegins.add(start)
                    tokenEnds.add(end)
                }
            }
        }
    }

    fun tokenBegin():Int {
        if (position < 0) {
            return 0
        } else if (position >= tokens.size) {
            return text.length
        } else {
            return tokenBegins[position]
        }
    }

    fun tokenEnd():Int {
        if (position < 0) {
            return 0
        } else if (position >= tokens.size) {
            return text.length
        } else {
            return tokenEnds[position]
        }
    }

    fun tokenText():String {
        return text.substring(tokenBegin(), tokenEnd())
    }

    fun mark() = Mark(this, position, log.mark(), tokenBegin())

    fun eof():Boolean = position + 1 >= tokens.size

    fun peek():LimaToken? {
        if (eof()) return null
        return tokens[position + 1]
    }

    fun next():LimaToken {
        if (eof()) throw IllegalStateException("Can't next, EOF")
        return tokens[position++]
    }

    fun match(token: LimaToken, error:String? = null):Boolean {
        if (peek() == token) {
            position++
            return true
        } else {
            if (error != null) {
                error(error)
            }
            return false
        }
    }

    fun error(message:String, position: Int = tokenBegin()) {
        log.error(position, message)
    }

    fun warn(message:String, position: Int = tokenBegin()) {
        log.warn(position, message)
    }

    fun info(message:String, position: Int = tokenBegin()) {
        log.info(position, message)
    }

    class Mark (val parser: TokenParser, val tokenMark:Int, val errorMark:Int, val position:Int) {

        fun rollback() {
            parser.position = tokenMark
            parser.log.rollback(errorMark)
        }
    }
}