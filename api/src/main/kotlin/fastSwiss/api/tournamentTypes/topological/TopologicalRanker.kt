package fastSwiss.api.tournamentTypes.topological

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.tournamentTypes.Ranker

/**
 * Сортирует с помощью топологической сортировки.
 */
class TopologicalRanker : Ranker<TopologicalRanking> {

    override fun generate(allPlayers: List<MutablePlayerState>): TopologicalRanking {
        val source = ArrayList<MutablePlayerState>(allPlayers)
        val res = ArrayList<MutablePlayerState>()

        var curRank = 1
        while (source.isNotEmpty()) {
            val minLossesCnt = source.minOf { player ->
                player.getLossesBalance(source)
            }

            val minRankSet = source.filter { player ->
                player.getLossesBalance(source) == minLossesCnt
            }.toSet()

            minRankSet.forEach { it.topSortRank = curRank }

            // TODO не реализована возможность назначения двум игрокам одного места
            // сортируем между собой по проценту побед в личных встречах, а при равенстве — по разнице сетов per match в личных встречах
            val minRankSortedList = ArrayList(minRankSet)
                .map { player ->
                    // результат в личных встречах: процент_побед, разница_сетов_per_match
                    val commonMatches = player.matchResults.values.filter { it.otherPlayer in minRankSet }
                    val matchesCnt = commonMatches.size
                    val winsCnt = commonMatches.count { it.isWin }
                    val setsDiff = commonMatches.sumOf { it.setsDiff }

                    player to if (matchesCnt == 0) {
                        0.5 to 0.0
                    } else {
                        winsCnt.toDouble() / matchesCnt to setsDiff.toDouble() / matchesCnt
                    }
                }
                .sortedByDescending {
                    it.second.first * 1_000_000 + it.second.second // TODO говнокод, конечно. Надо сравнивать первое и при равенстве второе
                }
                .map { it.first }


            res.addAll(minRankSortedList)
            source.removeAll(minRankSet)

            curRank++
        }

        return TopologicalRanking(res)
    }

}
