package fastSwiss.api.tournamentTypes.scoreAndBergerScore

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.tournamentTypes.Ranking
import kotlin.math.max

// TODO вынести общий код (например, код вывода матрицы игр) в суперкласс
class ScoreAndBergerScoreRanking(
    private val allPlayersSorted: List<MutablePlayerState>
) : Ranking {

    override fun outputRanking() {
        val maxNameLength = allPlayersSorted.maxOf { it.name.length } + 2
        print(
            "Место ".padEnd(6)
                    + "Игрок".padEnd(maxNameLength) + " "
                    + "Игр".padEnd(5)
                    + "Побед".padEnd(11)
                    + "Б-Побед".padEnd(11)
                    + "Сетов".padEnd(13)
                    + "Б-Сетов".padEnd(13)
        )
        repeat(allPlayersSorted.size) { idx -> print(" ${idx + 1}".padEnd(5)) }
        println()

        for ((index, player) in allPlayersSorted.withIndex()) {
            print(
                ("" + (index + 1) + ". ").padStart(6)
                        + player.name.padEnd(max(maxNameLength, "Игрок".length)) + " "
                        + (player.matchesFinishedCnt.toString() + if (player.isPlaysNow()) "*" else "").padEnd(5)
                        + (player.score.wins.toString() + " (%.2f)".format(player.score.winsAvg)).padEnd(11)
                        + (player.bergerScore.wins.toString() + " (%.2f)".format(player.bergerScore.winsAvg)).padEnd(11)
                        + ("%+d".format(player.score.setsDiff) + " (%+.2f)".format(player.score.setsDiffAvg)).padEnd(13)
                        + ("%+d".format(player.bergerScore.setsDiff) + " (%+.2f)".format(player.bergerScore.setsDiffAvg)).padEnd(13)
            )

            for ((otherIndex, otherPlayer) in allPlayersSorted.withIndex()) {
                val match = player.matchResults[otherPlayer]
                if (index == otherIndex) {
                    print(" X   ")
                } else if (match != null) {
                    val delimiter = if ((index > otherIndex) && (match.setsMy > match.setsOther)) {
                        "↑"
                    } else if ((index < otherIndex) && (match.setsMy < match.setsOther)) {
                        "↓"
                    } else {
                        ":"
                    }

                    print("${match.setsMy}$delimiter${match.setsOther}".padEnd(5))
                } else {
                    print(" •".padEnd(5))
                }
            }
            println()
        }
    }

}
