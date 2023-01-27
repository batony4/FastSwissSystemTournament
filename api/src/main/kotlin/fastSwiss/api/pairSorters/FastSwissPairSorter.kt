package fastSwiss.api.pairSorters

import fastSwiss.api.MutablePlayerState
import kotlin.math.abs

/**
 * Сортируем по близости игроков между собой по проценту побед с учётом невидимого гандикапа.
 */
class FastSwissPairSorter : PairSorter {

    override fun assessPair(player1: MutablePlayerState, player2: MutablePlayerState, allPlayers: Collection<MutablePlayerState>) =
        abs(player1.score.winsAvgWithHandicap - player2.score.winsAvgWithHandicap) +
                0.4 * (player1.matchesPlayed + player2.matchesPlayed)

}
