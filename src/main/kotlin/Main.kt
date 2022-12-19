import java.io.File
import java.io.PrintWriter
import java.util.*

private const val GO_TO_TABLE_PREFIX = "К СТОЛУ --> "

fun main() {
    val t = Tournament()

    val inputFile = File("tournament.txt")
    val sc = Scanner(inputFile)

    val outputFile = File("tournament_out.txt")
    val pw = PrintWriter(outputFile)

    while (sc.hasNextLine()) {
        val line = sc.nextLine().removePrefix(GO_TO_TABLE_PREFIX)
        pw.println(line)

        t.parseLine(line)
    }
    sc.close()

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
