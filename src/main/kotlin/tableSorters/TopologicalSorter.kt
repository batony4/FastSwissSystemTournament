package tableSorters

import PlayerState

class TopologicalSorter : Sorter {
    override fun sorted(allPlayers: List<PlayerState>): List<PlayerState> {
        val source = ArrayList<PlayerState>(allPlayers)
        val res = ArrayList<PlayerState>()

        while (source.isNotEmpty()) {
            val minLossesCnt = source.minOf { player ->
                player.matchResults.values
                    .filter { it.otherPlayer in source }
                    .count { !it.isWin }
            }

            val minRankList = source.filter { player ->
                player.matchResults.values
                    .filter { it.otherPlayer in source }
                    .count { !it.isWin } == minLossesCnt
            }.toSet()

            res.addAll(minRankList)
            source.removeAll(minRankList)

            // TODO не реализована сортировка таких игроков между собой по личным встречам, а также возможность назначения одного места
        }

        return res
    }
}
