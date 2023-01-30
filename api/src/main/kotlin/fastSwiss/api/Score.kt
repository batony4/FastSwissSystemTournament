package fastSwiss.api

/**
 * Текущий счёт участника.
 */
open class Score(
    val matchesPlayed: Int,
    val points: Int,
    val setsDiff: Int,
) {
    val pointsAvg = if (matchesPlayed > 0) points.toDouble() / matchesPlayed else (PlayerMatchResult.WIN_POINTS / 2.0)
    val setsDiffAvg = if (matchesPlayed > 0) setsDiff.toDouble() / matchesPlayed else 0.0
}
