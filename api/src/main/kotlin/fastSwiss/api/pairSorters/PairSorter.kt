package fastSwiss.api.pairSorters

import fastSwiss.api.MutablePlayerState

/**
 * Интерфейс для сортировки пар игроков, чтобы выбрать, какую пару поставить к столу.
 */
interface PairSorter {

    /**
     * Оценить, насколько это хорошая пара, чтобы её сейчас поставить к столу.
     * Чем меньше значение результата — тем лучше пара (и тем вероятнее, что она будет выбрана).
     */
    fun assessPair(player1: MutablePlayerState, player2: MutablePlayerState, allPlayers: Collection<MutablePlayerState>): Double
}
