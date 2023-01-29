package fastSwiss.api.tournamentTypes.topological

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.PlayerMatchResult
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
            // TODO при равенстве по личным встречам — сортировать по всем матчам (со всеми соперниками). затем — по рейтингу.
            //  Да и вообще, можно выделить в отдельные классы все дополнительные критерии сортировки,
            //  чтобы их можно было подставлять в любом порядке по желанию.
            val minRankSortedList = ArrayList(minRankSet)
                .map { player ->
                    // результат в личных встречах: процент_побед, разница_сетов_per_match
                    val commonMatches = player.matchResults.values.filter { it.otherPlayer in minRankSet }
                    val commonMatchesCnt = commonMatches.size
                    val commonPointsCnt = commonMatches.sumOf { it.pointsMy }
                    val commonSetsDiff = commonMatches.sumOf { it.setsDiff }

                    player to if (commonMatchesCnt == 0) {
                        (PlayerMatchResult.WIN_POINTS / 2.0) to 0.0
                    } else {
                        commonPointsCnt.toDouble() / commonMatchesCnt to commonSetsDiff.toDouble() / commonMatchesCnt
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
                p.pointsCnt,
                p.setsDiff,
                if (p.matchesFinishedCnt < p.handicapTours) p.handicapWins else 0,
                if (p.matchesFinishedCnt < p.handicapTours) p.handicapLosses else 0,
            )
        }

        return TopologicalRanking(res, topSortRank, score)
    }

}
