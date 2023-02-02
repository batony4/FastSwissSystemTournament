package fastSwiss.api.tournamentTypes.scoreAndBergerScore

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.Score
import fastSwiss.api.tournamentTypes.AbstractRanking
import kotlin.math.max

class ScoreAndBergerScoreRanking(
    val allPlayersSorted: List<MutablePlayerState>,
    val scoreWithHandicap: Map<MutablePlayerState, ScoreWithHandicap>,
    val bergerScore: Map<MutablePlayerState, Score>,
) : AbstractRanking() {

    override fun outputRanking(): String {
        val res = StringBuilder()
        val maxNameLength = allPlayersSorted.maxOf { it.name.length } + 2
        res.append(
            "Место ".padEnd(6)
                    + "Игрок".padEnd(maxNameLength) + " "
                    + "Игр".padEnd(5)
                    + "Очков".padEnd(11)
                    + "Б-Очков".padEnd(11)
                    + "Сетов".padEnd(13)
                    + "Б-Сетов".padEnd(13)
        )
        repeat(allPlayersSorted.size) { idx -> print(" ${idx + 1}".padEnd(5)) }
        res.appendLine()

        for ((index, player) in allPlayersSorted.withIndex()) {
            res.append(
                ("" + (index + 1) + ". ").padStart(6)
                        + player.name.padEnd(max(maxNameLength, "Игрок".length)) + " "
                        + (player.getMatchesFinishedCnt().toString() + if (player.isPlaysNow()) "*" else "").padEnd(5)
                        + (scoreWithHandicap[player]!!.points.toString() + " (%.2f)".format(scoreWithHandicap[player]!!.pointsAvg)).padEnd(
                    11
                )
                        + (bergerScore[player]!!.points.toString() + " (%.2f)".format(bergerScore[player]!!.pointsAvg)).padEnd(11)
                        + ("%+d".format(scoreWithHandicap[player]!!.setsDiff) + " (%+.2f)".format(scoreWithHandicap[player]!!.setsDiffAvg)).padEnd(
                    13
                )
                        + ("%+d".format(bergerScore[player]!!.setsDiff) + " (%+.2f)".format(bergerScore[player]!!.setsDiffAvg)).padEnd(13)
            )

            outputMatrixForPlayer(res, player, allPlayersSorted)
            res.appendLine()
        }

        return res.toString()
    }

}
