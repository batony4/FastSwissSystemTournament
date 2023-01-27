package fastSwiss.fileInterface

import fastSwiss.api.MutableTournament
import java.io.File
import java.io.PrintWriter

fun main() {
    val inputFile = File("tournament.txt")
    val outputFile = File("tournament_out.txt")

    val pw = PrintWriter(outputFile)
    val t = MutableTournament.parse(inputFile, pw)

    val allPlayersSorted = t.calcCurrentTable()

    while (t.hasFreeTables()) {
        t.generateAndStartMatch(pw) ?: break // не удалось поставить никого за стол — значит прерываем досрочно
    }
    pw.close()

    inputFile.delete()
    outputFile.renameTo(inputFile)

    t.outputCurrentTable(allPlayersSorted)

}
