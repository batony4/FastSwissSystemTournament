package fastSwiss.api.tournamentTypes.scoreAndBergerScore

import fastSwiss.api.PlayerMatchResult
import fastSwiss.api.Score

/**
 * Текущий счёт участника.
 */
class ScoreWithHandicap(
    matchesPlayed: Int,
    points: Int,
    setsDiff: Int,
    currentHandicapWins: Int,
    currentHandicapLosses: Int,
) : Score(matchesPlayed, points, setsDiff) {
    private val currentHandicapMatches = currentHandicapWins + currentHandicapLosses

    /** Результаты с учётом гандикапа. Нигде не отображаются в таблице, но учитываются при выборе пары в первых кругах */
    val pointsAvgWithHandicap =
        if (matchesPlayed + currentHandicapMatches > 0)
            (points.toDouble() + currentHandicapWins * PlayerMatchResult.WIN_POINTS) / (matchesPlayed + currentHandicapMatches)
        else
            (PlayerMatchResult.WIN_POINTS / 2.0)
}
