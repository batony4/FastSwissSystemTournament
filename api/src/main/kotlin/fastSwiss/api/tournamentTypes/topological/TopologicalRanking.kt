package fastSwiss.api.tournamentTypes.topological

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.Score
import fastSwiss.api.tournamentTypes.Ranking
import fastSwiss.api.tournamentTypes.Ranking.Companion.outputMatrixForPlayer
import kotlin.math.max

class TopologicalRanking(
    val allPlayersSorted: List<MutablePlayerState>,
    val topSortRank: Map<MutablePlayerState, Int>,
    val score: Map<MutablePlayerState, Score>,
) : Ranking {

    override fun outputRanking(shortNotFull: Boolean): String {
        val res = StringBuilder()
        val maxNameLength = allPlayersSorted.maxOf { it.name.length } + 2
        res.append(
            "Место ".padEnd(6)
                    + "Ранг "
                    + "Участник".padEnd(maxNameLength) + " "
                    + "Матчей".padEnd(7)
                    + "Очков".padEnd(11)
                    + "Счёт".padEnd(13)
        )
        repeat(allPlayersSorted.size) { idx -> res.append(" ${idx + 1}".padEnd(5)) }
        res.appendLine()

        for ((index, player) in allPlayersSorted.withIndex()) {
            res.append(
                ("" + (index + 1) + ". ").padStart(6)
                        + ("(${topSortRank[player]})").padStart(4) + " "
                        + player.name.padEnd(max(maxNameLength, "Участник".length)) + " "
                        + (player.getMatchesFinishedCnt().toString() + if (player.isPlaysNow()) "*" else "").padEnd(7)
                        + (score[player]!!.points.toString() + " (%.2f)".format(score[player]!!.pointsAvg)).padEnd(11)
                        + ("%+d".format(score[player]!!.setsDiff) + " (%+.2f)".format(score[player]!!.setsDiffAvg)).padEnd(13)
            )

            outputMatrixForPlayer(res, player, allPlayersSorted)
            res.appendLine()
        }

        return res.toString()
    }

}
