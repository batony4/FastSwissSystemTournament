package fastSwiss.api.tournamentTypes

import fastSwiss.api.MutablePlayerState

/**
 * Интерфейс для сортировки пар игроков, чтобы выбрать, какую пару поставить к столу.
 */
interface PairSorter<R : Ranking> {

    /**
     * Оценить, насколько это хорошая пара, чтобы её сейчас поставить к столу.
     * Чем меньше значение результата — тем лучше пара (и тем вероятнее, что она будет выбрана).
     * `null` означает, что эта пара сейчас не может быть поставлена к столу в любом случае.
     */
    fun assessPair(player1: MutablePlayerState, player2: MutablePlayerState, ranking: R): Double?
}
