package fastSwiss.api.tournamentTypes.scoreAndBergerScore

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.Score
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
class ScoreAndBergerScoreRanker : Ranker<ScoreAndBergerScoreRanking> {

    override fun generate(allPlayers: List<MutablePlayerState>): ScoreAndBergerScoreRanking {
        val score = allPlayers.associateWith { p ->
            Score(
                p.matchesFinishedCnt,
                p.pointsCnt,
                p.setsDiff,
                if (p.matchesFinishedCnt < p.handicapTours) p.handicapWins else 0,
                if (p.matchesFinishedCnt < p.handicapTours) p.handicapLosses else 0,
            )
        }

        val bergerScore = allPlayers.associateWith { p ->
            Score(
                p.matchResults.values.sumOf { score[it.otherPlayer]!!.matchesPlayed },
                p.matchResults.values.sumOf { score[it.otherPlayer]!!.points },
                p.matchResults.values.sumOf { score[it.otherPlayer]!!.setsDiff },
                0,
                0,
            )
        }

        val comparator = object : Comparator<MutablePlayerState> {
            override fun compare(o1: MutablePlayerState?, o2: MutablePlayerState?): Int {
                if (abs(score[o1]!!.pointsAvg - score[o2]!!.pointsAvg) > 1e-9) {
                    return -score[o1]!!.pointsAvg.compareTo(score[o2]!!.pointsAvg)
                }

                if (abs(bergerScore[o1]!!.pointsAvg - bergerScore[o2]!!.pointsAvg) > 1e-9) {
                    return -bergerScore[o1]!!.pointsAvg.compareTo(bergerScore[o2]!!.pointsAvg)
                }

                if (abs(score[o1]!!.setsDiffAvg - score[o2]!!.setsDiffAvg) > 1e-9) {
                    return -score[o1]!!.setsDiffAvg.compareTo(score[o2]!!.setsDiffAvg)
                }

                if (abs(bergerScore[o1]!!.setsDiffAvg - bergerScore[o2]!!.setsDiffAvg) > 1e-9) {
                    return -bergerScore[o1]!!.setsDiffAvg.compareTo(bergerScore[o2]!!.setsDiffAvg)
                }

                return 0
            }
        }

        return ScoreAndBergerScoreRanking(allPlayers.sortedWith(comparator), score, bergerScore)
    }

}
