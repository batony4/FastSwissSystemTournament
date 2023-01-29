package fastSwiss.api.tournamentTypes

import fastSwiss.api.MutablePlayerState

/**
 * Формирует текущую таблицу результатов на основе истории сыгранных матчей.
 */
interface Ranker<R : Ranking> {

    fun generate(allPlayers: List<MutablePlayerState>): R

}
