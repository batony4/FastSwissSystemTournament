package fastSwiss.api.tournamentTypes.topological

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.Score
import fastSwiss.api.tournamentTypes.AbstractRanking
import kotlin.math.max

class TopologicalRanking(
    val allPlayersSorted: List<MutablePlayerState>,
    val topSortRank: Map<MutablePlayerState, Int>,
    val score: Map<MutablePlayerState, Score>,
) : AbstractRanking() {

    override fun outputRanking() {
        val maxNameLength = allPlayersSorted.maxOf { it.name.length } + 2
        print(
            "Место ".padEnd(6)
                    + "Ранг "
                    + "Игрок".padEnd(maxNameLength) + " "
                    + "Игр".padEnd(5)
                    + "Побед".padEnd(11)
                    + "Сетов".padEnd(13)
        )
        repeat(allPlayersSorted.size) { idx -> print(" ${idx + 1}".padEnd(5)) }
        println()

        for ((index, player) in allPlayersSorted.withIndex()) {
            print(
                ("" + (index + 1) + ". ").padStart(6)
                        + ("(${topSortRank[player]})").padStart(4) + " "
                        + player.name.padEnd(max(maxNameLength, "Игрок".length)) + " "
                        + (player.matchesFinishedCnt.toString() + if (player.isPlaysNow()) "*" else "").padEnd(5)
                        + (score[player]!!.wins.toString() + " (%.2f)".format(score[player]!!.winsAvg)).padEnd(11)
                        + ("%+d".format(score[player]!!.setsDiff) + " (%+.2f)".format(score[player]!!.setsDiffAvg)).padEnd(13)
            )

            outputMatrixForPlayer(player, allPlayersSorted)
            println()
        }
    }

}
