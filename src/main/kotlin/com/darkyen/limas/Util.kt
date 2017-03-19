package com.darkyen.limas

/**
 * @return line at which given index is, lines indexed from 1
 */
fun CharSequence.lineOfIndex(of:Int):Int {
    return 1 + (0..(Math.min(of, length)-1)).count { this[it] == '\n' }
}

/**
 * @return column at which given index is, that is character from line start
 */
fun CharSequence.columnOfIndex(of:Int):Int {
    var lineStart = Math.min(of, length)
    while (lineStart > 0 && this[lineStart] != '\n') {
        lineStart--
    }
    return (of - lineStart)
}

fun CharSequence.previewOfLine(index:Int):CharSequence {
    var lineStart = Math.min(index - 1, length)
    while (true) {
        if (lineStart <= 0) {
            lineStart = 0
            break
        } else if (this[lineStart] == '\n') {
            lineStart += 1
            break
        }
        lineStart--
    }

    var lineEnd = Math.max(index, 0)
    while (lineEnd < length && this[lineEnd] != '\n') {
        lineEnd++
    }

    val sb = StringBuilder()
    sb.append(this, lineStart, lineEnd).append('\n')
    for (i in lineStart..lineEnd) {
        if (i == index) {
            sb.append('^')
        } else {
            sb.append(' ')
        }
    }

    return sb
}