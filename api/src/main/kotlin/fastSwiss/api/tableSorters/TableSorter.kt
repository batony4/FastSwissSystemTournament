package fastSwiss.api.tableSorters

import fastSwiss.api.MutablePlayerState

/**
 * Сортировщик текущей таблицы результатов.
 */
interface TableSorter {
    fun sorted(allPlayers: List<MutablePlayerState>): List<MutablePlayerState>
}
