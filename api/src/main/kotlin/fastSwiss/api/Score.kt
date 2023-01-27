package fastSwiss.api

import kotlin.math.abs

/**
 * Текущий счёт участника.
 */
// TODO сходу кажется, что это что-то сильно специфичное для ScoreAndBergerScoreTableSorter. подумать, может туда перенести получится
data class Score(
    val matchesPlayed: Int,
    val wins: Int,
    val setsDiff: Int,
    val currentHandicapWins: Int,
    val currentHandicapLosses: Int,
) : Comparable<Score> {
    val winsAvg = if (matchesPlayed > 0) wins.toDouble() / matchesPlayed else 0.5
    val setsDiffAvg = if (matchesPlayed > 0) setsDiff.toDouble() / matchesPlayed else 0.0
    private val currentHandicapMatches = currentHandicapWins + currentHandicapLosses

    /** Результаты с учётом гандикапа. Нигде не отображаются в таблице, но учитываются при выборе пары в первых кругах */
    val winsAvgWithHandicap =
        if (matchesPlayed + currentHandicapMatches > 0)
            (wins.toDouble() + currentHandicapWins) / (matchesPlayed + currentHandicapMatches)
        else
            0.5

    override fun compareTo(other: Score): Int {
        if (abs(winsAvg - other.winsAvg) > 1e-9) {
            return -winsAvg.compareTo(other.winsAvg)
        }
        if (abs(setsDiffAvg - other.setsDiffAvg) > 1e-9) {
            return -setsDiffAvg.compareTo(other.setsDiffAvg)
        }

        return 0
    }
}
