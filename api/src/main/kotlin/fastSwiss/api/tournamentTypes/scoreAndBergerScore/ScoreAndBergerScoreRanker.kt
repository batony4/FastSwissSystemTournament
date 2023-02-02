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
        val scoreWithHandicap = allPlayers.associateWith { p ->
            ScoreWithHandicap(
                p.getMatchesFinishedCnt(),
                p.getPointsCnt(),
                p.getSetsDiff(),
                if (p.getMatchesFinishedCnt() < p.handicapTours) p.handicapWins else 0,
                if (p.getMatchesFinishedCnt() < p.handicapTours) p.handicapLosses else 0,
            )
        }

        val bergerScore = allPlayers.associateWith { p ->
            Score(
                p.matchResults.values.sumOf { scoreWithHandicap[it.otherPlayer]!!.matchesPlayed },
                p.matchResults.values.sumOf { scoreWithHandicap[it.otherPlayer]!!.points },
                p.matchResults.values.sumOf { scoreWithHandicap[it.otherPlayer]!!.setsDiff },
            )
        }

        val comparator = object : Comparator<MutablePlayerState> {
            override fun compare(o1: MutablePlayerState?, o2: MutablePlayerState?): Int {
                if (abs(scoreWithHandicap[o1]!!.pointsAvg - scoreWithHandicap[o2]!!.pointsAvg) > 1e-9) {
                    return -scoreWithHandicap[o1]!!.pointsAvg.compareTo(scoreWithHandicap[o2]!!.pointsAvg)
                }

                if (abs(bergerScore[o1]!!.pointsAvg - bergerScore[o2]!!.pointsAvg) > 1e-9) {
                    return -bergerScore[o1]!!.pointsAvg.compareTo(bergerScore[o2]!!.pointsAvg)
                }

                if (abs(scoreWithHandicap[o1]!!.setsDiffAvg - scoreWithHandicap[o2]!!.setsDiffAvg) > 1e-9) {
                    return -scoreWithHandicap[o1]!!.setsDiffAvg.compareTo(scoreWithHandicap[o2]!!.setsDiffAvg)
                }

                if (abs(bergerScore[o1]!!.setsDiffAvg - bergerScore[o2]!!.setsDiffAvg) > 1e-9) {
                    return -bergerScore[o1]!!.setsDiffAvg.compareTo(bergerScore[o2]!!.setsDiffAvg)
                }

                return 0
            }
        }

        return ScoreAndBergerScoreRanking(allPlayers.sortedWith(comparator), scoreWithHandicap, bergerScore)
    }

}
