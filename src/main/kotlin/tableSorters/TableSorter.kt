package tableSorters

import PlayerState

interface TableSorter {
    fun sorted(allPlayers: List<PlayerState>): List<PlayerState>
}
