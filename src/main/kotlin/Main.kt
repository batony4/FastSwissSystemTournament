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

    outputTable(t.getAllPlayers())

}

fun outputTable(allPlayers: Collection<Player>) {
    val allPlayersSorted = allPlayers.sorted()
    val maxNameLength = allPlayers.maxOf { it.name.length } + 2
    print(
        "Место ".padEnd(6)
                + "Игрок".padEnd(maxNameLength)
                + "Игр".padEnd(5)
                + "Побед".padEnd(11)
                + "Сетов".padEnd(13)
                + "Б-Побед".padEnd(11)
                + "Б-Сетов".padEnd(13)
    )
    repeat(allPlayersSorted.size) { idx -> print(" ${idx + 1}".padEnd(5)) }
    println()

    for ((index, player) in allPlayersSorted.withIndex()) {
        print(
            ("" + (index + 1) + ". ").padStart(6)
                    + player.name.padEnd(maxNameLength)
                    + (player.matchesPlayed.toString() + if (player.isPlaysNow()) "*" else "").padEnd(5)
                    + (player.score.wins.toString() + " (%.2f)".format(player.score.winsAvg)).padEnd(11)
                    + ("%+d".format(player.score.setsDiff) + " (%+.2f)".format(player.score.setsDiffAvg)).padEnd(13)
                    + (player.bergerScore.wins.toString() + " (%.2f)".format(player.bergerScore.winsAvg)).padEnd(11)
                    + ("%+d".format(player.bergerScore.setsDiff) + " (%+.2f)".format(player.bergerScore.setsDiffAvg)).padEnd(13)
        )

        for ((otherIndex, otherPlayer) in allPlayersSorted.withIndex()) {
            val match = player.matchResults[otherPlayer.name]
            if (index == otherIndex) {
                print(" X   ")
            } else if (match != null) {
                print("${match.setsMy}:${match.setsOther}".padEnd(5))
            } else {
                print(" •".padEnd(5))
            }
        }
        println()
    }
}
