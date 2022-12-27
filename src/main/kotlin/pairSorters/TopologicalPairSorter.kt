package pairSorters

import PlayerState
import kotlin.math.abs

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

    override fun assessPair(player1: PlayerState, player2: PlayerState, allPlayers: Collection<PlayerState>): Double {
        val maxRankDiff = allPlayers.maxOf { it.topSortRank!! } - 1
        return abs(player1.topSortRank!! - player2.topSortRank!!) +
                0.41 * maxRankDiff * (player1.matchesPlayed + player2.matchesPlayed)
    }

}
