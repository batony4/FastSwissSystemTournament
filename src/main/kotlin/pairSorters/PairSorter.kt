package pairSorters

import PlayerState

interface PairSorter {

    /**
     * Оценить, насколько это хорошая пара, чтобы её сейчас поставить к столу.
     * Чем меньше значение результата — тем лучше пара (и тем вероятнее, что она будет выбрана).
     */
    fun assessPair(player1: PlayerState, player2: PlayerState, allPlayers: Collection<PlayerState>): Double
}
