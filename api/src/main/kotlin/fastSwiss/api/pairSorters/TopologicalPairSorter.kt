package fastSwiss.api.pairSorters

import fastSwiss.api.PlayerState

/**
 * Сортируем по близости рангов игроков в топологической сортировке.
 */
// TODO
//  1. Несравнимые лучше сравнимых. Даже независимо от ранга.
//	 Несравнимые — значит с учётом транзитивности их результат между собой неясен.
//	 Более формально: между игроками нет пути по направленным рёбрам (направление — от победителя к проигравшему в матче).
//   (пока без этого. может, окажется, что это не обязательно)
//  2. Минимизация текущей разницы рангов
class TopologicalPairSorter : PairSorter {

    // TODO можно использовать другой алгоритм в самом конце турнира, а то жадник работает так, что остаются пары из игрока с самого
    //  начала таблицы с игроком из самого конца. Лучше использовать не жадник, а продумывать все пары сразу, когда осталось всего
    //  несколько игр до конца
    override fun assessPair(player1: PlayerState, player2: PlayerState, allPlayers: Collection<PlayerState>): Double {
        val maxRankDiffSqr = sqr(allPlayers.maxOf { it.topSortRank!! } - 1)
        val minMatchesPlayed = allPlayers.minOf { it.matchesPlayed }
        return sqr(player1.topSortRank!! - player2.topSortRank!!).toDouble() / maxRankDiffSqr +
                0.17 * (sqr(player1.matchesPlayed - minMatchesPlayed) + sqr(player2.matchesPlayed - minMatchesPlayed))
    }

    private fun sqr(x: Int) = x * x

}
