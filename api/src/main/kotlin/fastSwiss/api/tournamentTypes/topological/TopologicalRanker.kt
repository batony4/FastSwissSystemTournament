package fastSwiss.api.tournamentTypes.topological

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.Score
import fastSwiss.api.tournamentTypes.Ranker

/**
 * Сортирует с помощью топологической сортировки.
 */
class TopologicalRanker : Ranker<TopologicalRanking> {

    /**
     * Количество поражений от игроков из [filteredPlayers] минус количество побед у игроков не из [filteredPlayers].
     */
    private fun getLossesBalance(p: MutablePlayerState, filteredPlayers: ArrayList<MutablePlayerState>): Int {
        // количество поражений от оставшихся в рассмотрении
        val lossesInSourceCnt = p.matchResults.values
            .filter { it.otherPlayer in filteredPlayers }
            .count { !it.isWin }

        // количество побед у выбывших из рассмотрения
        val winsNotInSourceCnt = p.matchResults.values
            .filter { it.otherPlayer !in filteredPlayers }
            .count { it.isWin }

        return lossesInSourceCnt - winsNotInSourceCnt
    }

    override fun generate(allPlayers: List<MutablePlayerState>): TopologicalRanking {
        val source = ArrayList<MutablePlayerState>(allPlayers)
        val res = ArrayList<MutablePlayerState>()

        val topSortRank = HashMap<MutablePlayerState, Int>()

        var curRank = 1
        while (source.isNotEmpty()) {
            val minLossesCnt = source.minOf { player ->
                getLossesBalance(player, source)
            }

            val minRankSet = source.filter { player ->
                getLossesBalance(player, source) == minLossesCnt
            }.toSet()

            minRankSet.forEach { topSortRank[it] = curRank }

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
                .sortedWith { o1, o2 ->
                    val rankWinsDiff = o2.second.first.compareTo(o1.second.first)
                    if (rankWinsDiff != 0) {
                        rankWinsDiff
                    } else {
                        o2.second.second.compareTo(o1.second.second)
                    }
                }
                .map { it.first }


            res.addAll(minRankSortedList)
            source.removeAll(minRankSet)

            curRank++
        }

        val score = allPlayers.associateWith { p ->
            Score(
                p.matchesFinishedCnt,
                p.matchesWonCnt,
                p.setsDiff,
                if (p.matchesFinishedCnt < p.handicapTours) p.handicapWins else 0,
                if (p.matchesFinishedCnt < p.handicapTours) p.handicapLosses else 0,
            )
        }

        return TopologicalRanking(res, topSortRank, score)
    }

}
