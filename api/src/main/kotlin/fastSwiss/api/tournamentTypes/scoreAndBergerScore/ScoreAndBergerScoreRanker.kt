package fastSwiss.api.tournamentTypes.scoreAndBergerScore

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.tournamentTypes.Ranker
import kotlin.math.abs

/**
 * Сортирует не по количеству (побед, сетов и так далее), а по проценту — так как у разных игроков
 * может быть разное количество сыгранных матчей.
 * Критерии сортировки:
 * - процент побед
 * - процент побед у всех соперников данного игрока (критерий Бергера)
 * - баланс выигранных сетов
 * - баланс выигранных сетов у всех соперников данного игрока (второй критерий Бергера)
 */
class ScoreAndBergerScoreRanker : Comparator<MutablePlayerState>, Ranker<ScoreAndBergerScoreRanking> {
    override fun compare(o1: MutablePlayerState, o2: MutablePlayerState): Int {

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

    override fun generate(allPlayers: List<MutablePlayerState>): ScoreAndBergerScoreRanking {
        return ScoreAndBergerScoreRanking(allPlayers.sortedWith(this))
    }

}
