package fastSwiss.pairSorters

import fastSwiss.PlayerState
import kotlin.math.abs

/**
 * Сортируем по близости игроков между собой по проценту побед с учётом невидимого гандикапа.
 */
class FastSwissPairSorter : PairSorter {

    override fun assessPair(player1: PlayerState, player2: PlayerState, allPlayers: Collection<PlayerState>) =
        abs(player1.score.winsAvgWithHandicap - player2.score.winsAvgWithHandicap) +
                0.4 * (player1.matchesPlayed + player2.matchesPlayed)

}
