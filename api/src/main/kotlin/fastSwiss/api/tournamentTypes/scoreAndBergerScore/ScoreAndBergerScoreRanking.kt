package fastSwiss.api.tournamentTypes.scoreAndBergerScore

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.Score
import fastSwiss.api.tournamentTypes.Ranking
import kotlin.math.max

// TODO вынести общий код (например, код вывода матрицы игр) в суперкласс
class ScoreAndBergerScoreRanking(
    val allPlayersSorted: List<MutablePlayerState>,
    val score: Map<MutablePlayerState, Score>,
    val bergerScore: Map<MutablePlayerState, Score>,
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
                        + (score[player]!!.wins.toString() + " (%.2f)".format(score[player]!!.winsAvg)).padEnd(11)
                        + (bergerScore[player]!!.wins.toString() + " (%.2f)".format(bergerScore[player]!!.winsAvg)).padEnd(11)
                        + ("%+d".format(score[player]!!.setsDiff) + " (%+.2f)".format(score[player]!!.setsDiffAvg)).padEnd(13)
                        + ("%+d".format(bergerScore[player]!!.setsDiff) + " (%+.2f)".format(bergerScore[player]!!.setsDiffAvg)).padEnd(13)
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
