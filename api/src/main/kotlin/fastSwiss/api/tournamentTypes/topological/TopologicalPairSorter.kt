package fastSwiss.api.tournamentTypes.topological

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.tournamentTypes.PairSorter

/**
 * Сортируем по близости рангов игроков в топологической сортировке.
 */
// TODO
//  1. Несравнимые лучше сравнимых. Даже независимо от ранга.
//	 Несравнимые — значит с учётом транзитивности их результат между собой неясен.
//	 Более формально: между игроками нет пути по направленным рёбрам (направление — от победителя к проигравшему в матче).
//   (пока без этого. может, окажется, что это не обязательно)
//  2. Минимизация текущей разницы рангов
class TopologicalPairSorter : PairSorter<TopologicalRanking> {

    // TODO можно использовать другой алгоритм в самом конце турнира, а то жадник работает так, что остаются пары из игрока с самого
    //  начала таблицы с игроком из самого конца. Лучше использовать не жадник, а продумывать все пары сразу, когда осталось всего
    //  несколько игр до конца
    override fun assessPair(player1: MutablePlayerState, player2: MutablePlayerState, ranking: TopologicalRanking): Double {
        val maxRankDiffSqr = sqr(ranking.allPlayersSorted.maxOf { ranking.topSortRank[it]!! } - 1)
        val minMatchesPlayed = ranking.allPlayersSorted.minOf { it.matchesFinishedCnt }
        return sqr(ranking.topSortRank[player1]!! - ranking.topSortRank[player2]!!).toDouble() / maxRankDiffSqr +
                0.17 * (sqr(player1.matchesFinishedCnt - minMatchesPlayed) + sqr(player2.matchesFinishedCnt - minMatchesPlayed))
    }

    private fun sqr(x: Int) = x * x

}
