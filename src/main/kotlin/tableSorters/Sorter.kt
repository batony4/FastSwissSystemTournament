package tableSorters

import PlayerState

interface Sorter {
    fun sorted(allPlayers: List<PlayerState>): List<PlayerState>
}
