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
    var lineStart = Math.min(index, length)
    while (lineStart > 0 && this[lineStart] != '\n') {
        lineStart--
    }
    lineStart++

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

/*
override fun toString(): String {
        val sb = StringBuilder()
        for (i in (pos-20..pos-1)) {
            if (i >= 0 && i < data.size) {
                sb.append(data[i])
            }
        }
        sb.append(" | ")
        for (i in (pos..pos+20)) {
            if (i >= 0 && i < data.size) {
                sb.append(data[i])
            }
        }

        if (eof()) {
            sb.append(" [EOF]")
        }

        return sb.toString()
    }
 */