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

    fun getMatchesFinishedCnt() = matchResults.size
    fun getMatchesStartedCnt() = getMatchesFinishedCnt() + (if (activeMatchWith != null) 1 else 0)

    fun getMatchesWonCnt() = matchResults.values.sumOf { it.winsMy }
    fun getMatchesDrawnCnt() = matchResults.values.sumOf { it.drawsMy }
    fun getPointsCnt() = matchResults.values.sumOf { it.pointsMy }
    fun getSetsDiff() = matchResults.values.sumOf { it.setsDiff }

    fun isStartedTournament() = getMatchesStartedCnt() > 0

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
