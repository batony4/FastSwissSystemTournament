import Tournament.Companion.GO_TO_TABLE_PREFIX
import java.io.File
import java.io.PrintWriter

fun main() {
    val inputFile = File("tournament.txt")

    val outputFile = File("tournament_out.txt")
    val pw = PrintWriter(outputFile)

    val t = Tournament.parse(inputFile, pw)

    while (t.hasFreeTables()) {
        t.generateAndStartMatch()?.let { match ->
            pw.println("$GO_TO_TABLE_PREFIX${match.first.name} ${match.second.name}")
        } ?: break // не удалось поставить никого за стол — значит прерываем досрочно
    }
    pw.close()

    inputFile.delete()
    outputFile.renameTo(inputFile)

    t.outputCurrentTable()
}
