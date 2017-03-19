import com.darkyen.limas.AssemblyParser
import com.darkyen.limas.ErrorContext
import java.io.FileReader

/**
 *
 */

fun main(args: Array<String>) {
    val errorContext = ErrorContext("offsets.lima")
    val parser = AssemblyParser(FileReader("offsets.lima").readText(), errorContext)

    while (!parser.eof()) {
        val token = parser.next()
        val text = parser.tokenText()

        println("\"$text\" - $token")
    }
}