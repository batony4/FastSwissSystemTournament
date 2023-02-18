package fastSwiss.api.tournamentTypes.topological

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.PlayerMatchResult
import fastSwiss.api.Score
import fastSwiss.api.tournamentTypes.Ranker

/**
 * Сортирует с помощью топологической сортировки.
 * В случае равенства рангов — сортирует по проценту побед в личных встречах, а при равенстве — по разнице сетов per match в личных встречах.
 * Если и здесь равенство, то применяется процент побед и разница сетов per match соответственно во всех матчах.
 */
class TopologicalRanker : Ranker<TopologicalRanking> {

    /**
     * Количество поражений от игроков из [filteredPlayers] минус количество побед у игроков не из [filteredPlayers].
     */
    private fun getLossesBalance(p: MutablePlayerState, filteredPlayers: ArrayList<MutablePlayerState>): Int {
        // количество потерь очков от оставшихся в рассмотрении
        val pointsLossInSourceCnt = p.matchResults.values
            .filter { it.otherPlayer in filteredPlayers }
            .sumOf { PlayerMatchResult.WIN_POINTS - it.pointsMy }

        // количество заработанных очков в матчах с выбывшими из рассмотрения
        val pointsWonNotInSourceCnt = p.matchResults.values
            .filter { it.otherPlayer !in filteredPlayers }
            .sumOf { it.pointsMy }

        return pointsLossInSourceCnt - pointsWonNotInSourceCnt
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

            // сортируем между собой по проценту побед в личных встречах, а при равенстве — по разнице сетов per match в личных встречах
            val minRankSortedList = ArrayList(minRankSet)
                .map { player ->
                    // результат в личных встречах: процент_побед, разница_сетов_per_match
                    val allMatches = player.matchResults.values
                    val allMatchesCnt = allMatches.size
                    val allPointsCnt = allMatches.sumOf { it.pointsMy }
                    val allSetsDiff = allMatches.sumOf { it.setsDiff }
                    val allScore = Score(allMatchesCnt, allPointsCnt, allSetsDiff)

                    val commonMatches = allMatches.filter { it.otherPlayer in minRankSet }
                    val commonMatchesCnt = commonMatches.size
                    val commonPointsCnt = commonMatches.sumOf { it.pointsMy }
                    val commonSetsDiff = commonMatches.sumOf { it.setsDiff }
                    val commonScore = Score(commonMatchesCnt, commonPointsCnt, commonSetsDiff)

                    player to (commonScore to allScore)
                }
                .sortedWith { o1, o2 ->
                    // сравниваем сначала по матчам между собой, затем по всем матчам
                    val commonDiff = o1.second.first.compareTo(o2.second.first)
                    if (commonDiff != 0) {
                        commonDiff
                    } else {
                        o1.second.second.compareTo(o2.second.second)
                    }
                }
                .map { it.first }


            res.addAll(minRankSortedList)
            source.removeAll(minRankSet)

            curRank++
        }

        val score = allPlayers.associateWith { p ->
            Score(
                p.getMatchesFinishedCnt(),
                p.getPointsCnt(),
                p.getSetsDiff(),
            )
        }

        return TopologicalRanking(res, topSortRank, score)
    }

}
