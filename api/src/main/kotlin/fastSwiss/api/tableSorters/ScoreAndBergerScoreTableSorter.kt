package fastSwiss.api.tableSorters

import fastSwiss.api.PlayerState
import kotlin.math.abs

class ScoreAndBergerScoreTableSorter : Comparator<PlayerState>, TableSorter {
    override fun compare(o1: PlayerState, o2: PlayerState): Int {

        if (abs(o1.score.winsAvg - o2.score.winsAvg) > 1e-9) {
            return -o1.score.winsAvg.compareTo(o2.score.winsAvg)
        }

        if (abs(o1.bergerScore.winsAvg - o2.bergerScore.winsAvg) > 1e-9) {
            return -o1.bergerScore.winsAvg.compareTo(o2.bergerScore.winsAvg)
        }

        if (abs(o1.score.setsDiffAvg - o2.score.setsDiffAvg) > 1e-9) {
            return -o1.score.setsDiffAvg.compareTo(o2.score.setsDiffAvg)
        }

        if (abs(o1.bergerScore.setsDiffAvg - o2.bergerScore.setsDiffAvg) > 1e-9) {
            return -o1.bergerScore.setsDiffAvg.compareTo(o2.bergerScore.setsDiffAvg)
        }

        return 0
    }

    override fun sorted(allPlayers: List<PlayerState>): List<PlayerState> {
        return allPlayers.sortedWith(this)
    }

}
