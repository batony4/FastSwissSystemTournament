/**
 * Игрок, включая его статистику на турнире.
 */
class PlayerState(
    val name: String,
    private val handicapTours: Int,
    private val handicapWins: Int,
    private val handicapLosses: Int,
) : Comparable<PlayerState> {

    private var activeMatchWith: PlayerState? = null // игрок, с которым сейчас идёт матч
    val matchResults = HashMap<PlayerState, PlayerMatchResult>()

    val matchesPlayed by lazy { matchResults.size }
    private val matchesWon by lazy { matchResults.values.sumOf { it.winsMy } }
    private val setsDiff by lazy { matchResults.values.sumOf { it.setsMy - it.setsOther } }

    val score by lazy {
        Score(
            matchesPlayed,
            matchesWon,
            setsDiff,
            if (matchesPlayed < handicapTours) handicapWins else 0,
            if (matchesPlayed < handicapTours) handicapLosses else 0,
        )
    }

    val bergerScore by lazy {
        Score(
            matchResults.values.sumOf { it.otherPlayer.score.matchesPlayed },
            matchResults.values.sumOf { it.otherPlayer.score.wins },
            matchResults.values.sumOf { it.otherPlayer.score.setsDiff },
            0,
            0,
        )
    }

    fun isPlaysNow() = activeMatchWith != null

    fun isFinishedGameWith(otherPlayer: PlayerState) = matchResults.containsKey(otherPlayer)

    fun startMatchWith(otherPlayer: PlayerState) {
        if (activeMatchWith != null) {
            throw IllegalStateException("Уже играем с $activeMatchWith")
        }
        activeMatchWith = otherPlayer
    }

    fun endMatch(setsMy: Int, setsOther: Int) {
        activeMatchWith?.let { otherPlayer ->
            matchResults[otherPlayer] = PlayerMatchResult(otherPlayer, setsMy, setsOther)
            activeMatchWith = null
        } ?: throw IllegalStateException("Не играем сейчас")
    }

    override fun compareTo(other: PlayerState): Int {
        val scoreCompare = score.compareTo(other.score)
        if (scoreCompare != 0) return scoreCompare

        return bergerScore.compareTo(other.bergerScore)
    }

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerState) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}
