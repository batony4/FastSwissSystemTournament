import kotlin.math.abs

data class Score(
    val matchesPlayed: Int,
    val wins: Int,
    val setsDiff: Int,

    val bergerMatchesPlayed: Int,
    val bergerWins: Int,
    val bergerSetsDiff: Int,
): Comparable<Score> {
    val winsAvg = if (matchesPlayed > 0) wins.toDouble() / matchesPlayed else 0.5
    val setsDiffAvg = if (matchesPlayed > 0) setsDiff.toDouble() / matchesPlayed else 0.0

    val bergerWinsAvg = if (bergerMatchesPlayed > 0) bergerWins.toDouble() / bergerMatchesPlayed else 0.5
    val bergerSetsDiffAvg = if (bergerMatchesPlayed > 0) bergerSetsDiff.toDouble() / bergerMatchesPlayed else 0.0

    override fun compareTo(other: Score): Int {
        if (abs(winsAvg - other.winsAvg) < 1e-9) {
            return winsAvg.compareTo(other.winsAvg)
        }
        if (abs(setsDiffAvg - other.setsDiffAvg) < 1e-9) {
            return setsDiffAvg.compareTo(other.setsDiffAvg)
        }

        if (abs(bergerWinsAvg - other.bergerWinsAvg) < 1e-9) {
            return bergerWinsAvg.compareTo(other.bergerWinsAvg)
        }

        if (abs(bergerSetsDiffAvg - other.bergerSetsDiffAvg) < 1e-9) {
            return bergerSetsDiffAvg.compareTo(other.bergerSetsDiffAvg)
        }

        return 0
    }
}
