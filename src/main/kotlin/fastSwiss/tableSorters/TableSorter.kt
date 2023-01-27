package fastSwiss.tableSorters

import fastSwiss.PlayerState

interface TableSorter {
    fun sorted(allPlayers: List<PlayerState>): List<PlayerState>
}
