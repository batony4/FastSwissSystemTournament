package fastSwiss.api.tournamentTypes.topological

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.tournamentTypes.PairSorter

/**
 * Сортируем по близости рангов игроков в топологической сортировке.
 */
class TopologicalPairSorter : PairSorter<TopologicalRanking> {

    override fun assessPair(player1: MutablePlayerState, player2: MutablePlayerState, ranking: TopologicalRanking): Double {
        val maxRankDiffSqr = sqr(ranking.allPlayersSorted.maxOf { ranking.topSortRank[it]!! } - 1)
        val minMatchesPlayed = ranking.allPlayersSorted.minOf { it.matchesFinishedCnt }
        return sqr(ranking.topSortRank[player1]!! - ranking.topSortRank[player2]!!).toDouble() / maxRankDiffSqr +
                0.17 * (sqr(player1.matchesFinishedCnt - minMatchesPlayed) + sqr(player2.matchesFinishedCnt - minMatchesPlayed))
    }

    private fun sqr(x: Int) = x * x

}
