package fastSwiss.api

import kotlin.math.abs

/**
 * Текущий счёт участника.
 */
data class Score(
    val matchesPlayed: Int,
    val points: Int,
    val setsDiff: Int,
    // TODO подумать, где должна быть логика гандикапа; не относится ли она больше именно к ScoreAndBergerScore?
    val currentHandicapWins: Int,
    val currentHandicapLosses: Int,
) : Comparable<Score> {
    val pointsAvg = if (matchesPlayed > 0) points.toDouble() / matchesPlayed else (PlayerMatchResult.WIN_POINTS / 2.0)
    val setsDiffAvg = if (matchesPlayed > 0) setsDiff.toDouble() / matchesPlayed else 0.0
    private val currentHandicapMatches = currentHandicapWins + currentHandicapLosses

    /** Результаты с учётом гандикапа. Нигде не отображаются в таблице, но учитываются при выборе пары в первых кругах */
    val pointsAvgWithHandicap =
        if (matchesPlayed + currentHandicapMatches > 0)
            (points.toDouble() + currentHandicapWins * PlayerMatchResult.WIN_POINTS) / (matchesPlayed + currentHandicapMatches)
        else
            (PlayerMatchResult.WIN_POINTS / 2.0)

    override fun compareTo(other: Score): Int {
        if (abs(pointsAvg - other.pointsAvg) > 1e-9) {
            return -pointsAvg.compareTo(other.pointsAvg)
        }
        if (abs(setsDiffAvg - other.setsDiffAvg) > 1e-9) {
            return -setsDiffAvg.compareTo(other.setsDiffAvg)
        }

        return 0
    }
}
