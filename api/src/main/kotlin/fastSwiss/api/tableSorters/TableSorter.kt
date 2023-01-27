package fastSwiss.api.tableSorters

import fastSwiss.api.PlayerState

interface TableSorter {
    fun sorted(allPlayers: List<PlayerState>): List<PlayerState>
}
