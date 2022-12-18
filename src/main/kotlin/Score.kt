import kotlin.math.abs

/**
 * Текущий счёт участника.
 */
data class Score(
    val matchesPlayed: Int,
    val wins: Int,
    val setsDiff: Int,
): Comparable<Score> {
    val winsAvg = if (matchesPlayed > 0) wins.toDouble() / matchesPlayed else 0.5
    val setsDiffAvg = if (matchesPlayed > 0) setsDiff.toDouble() / matchesPlayed else 0.0

    override fun compareTo(other: Score): Int {
        if (abs(winsAvg - other.winsAvg) > 1e-9) {
            return winsAvg.compareTo(other.winsAvg)
        }
        if (abs(setsDiffAvg - other.setsDiffAvg) > 1e-9) {
            return setsDiffAvg.compareTo(other.setsDiffAvg)
        }

        return 0
    }
}
