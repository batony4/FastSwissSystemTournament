package fastSwiss.api

/**
 * Игрок, включая его статистику на турнире.
 */
class MutablePlayerState(
    override val name: String,
    initialIsPaused: Boolean,
    override val handicapTours: Int,
    override val handicapWins: Int,
    override val handicapLosses: Int,
) : ImmutablePlayerState {

    override var isPaused = initialIsPaused
    var activeMatchWith: MutablePlayerState? = null // игрок, с которым сейчас идёт матч
        private set
    val matchResults = HashMap<MutablePlayerState, PlayerMatchResult>()

    val matchesFinishedCnt by lazy { matchResults.size }
    val matchesStartedCnt by lazy { matchesFinishedCnt + (if (activeMatchWith != null) 1 else 0) }

    val matchesWonCnt by lazy { matchResults.values.sumOf { it.winsMy } }
    val matchesDrawnCnt by lazy { matchResults.values.sumOf { it.drawsMy } }
    val pointsCnt by lazy { matchResults.values.sumOf { it.pointsMy } }
    val setsDiff by lazy { matchResults.values.sumOf { it.setsDiff } }

    fun isStartedTournament() = matchesStartedCnt > 0

    fun getAllPlayersPlayedOrStarted(): List<MutablePlayerState> {
        val res = ArrayList(matchResults.keys)
        if (activeMatchWith != null) res += activeMatchWith
        return res
    }

    override fun isPlaysNow() = activeMatchWith != null

    fun isFinishedGameWith(otherPlayer: MutablePlayerState) = matchResults.containsKey(otherPlayer)

    fun startMatchWith(otherPlayer: MutablePlayerState) {
        if (activeMatchWith != null) throw IllegalStateException("Уже играем с $activeMatchWith")
        if (matchResults.containsKey(otherPlayer)) throw IllegalStateException("Уже сыграли ранее с $otherPlayer")

        activeMatchWith = otherPlayer
    }

    fun endMatch(setsMy: Int, setsOther: Int) {
        activeMatchWith?.let { otherPlayer ->
            matchResults[otherPlayer] = PlayerMatchResult(otherPlayer, setsMy, setsOther)
            activeMatchWith = null
        } ?: throw IllegalStateException("Не играем сейчас")
    }

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MutablePlayerState) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}
