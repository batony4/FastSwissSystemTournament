package fastSwiss.api.tournamentTypes.scoreAndBergerScore

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.tournamentTypes.PairSorter
import kotlin.math.abs

/**
 * Сортируем по близости игроков между собой по проценту побед с учётом невидимого гандикапа.
 */
class ScoreAndBergerScorePairSorter : PairSorter<ScoreAndBergerScoreRanking> {

    override fun assessPair(player1: MutablePlayerState, player2: MutablePlayerState, ranking: ScoreAndBergerScoreRanking) =
        abs(ranking.score[player1]!!.pointsAvgWithHandicap - ranking.score[player2]!!.pointsAvgWithHandicap) +
                0.4 * (player1.matchesFinishedCnt + player2.matchesFinishedCnt)

}
