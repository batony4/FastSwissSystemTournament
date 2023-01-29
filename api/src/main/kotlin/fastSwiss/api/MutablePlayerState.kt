package fastSwiss.api

/**
 * Игрок, включая его статистику на турнире.
 */
// TODO добавить больше защиты от неправильных действий, так как в Телеграм-Боте это уже актуально.
class MutablePlayerState(
    val name: String,
    initialIsPaused: Boolean,
    val handicapTours: Int,
    val handicapWins: Int,
    val handicapLosses: Int,
) {

    var isPaused = initialIsPaused
    private var activeMatchWith: MutablePlayerState? = null // игрок, с которым сейчас идёт матч
    val matchResults = HashMap<MutablePlayerState, PlayerMatchResult>()

    val matchesFinishedCnt by lazy { matchResults.size }
    val matchesStartedCnt by lazy { matchesFinishedCnt + (if (activeMatchWith != null) 1 else 0) }

    // TODO поддержать везде ничьи (по всему коду пройтись)
    val matchesWonCnt by lazy { matchResults.values.sumOf { it.winsMy } }
    private val drawsCnt by lazy { matchResults.values.sumOf { it.drawsMy } }
    val setsDiff by lazy { matchResults.values.sumOf { it.setsDiff } }

    fun getAllPlayersPlayedOrStarted(): List<MutablePlayerState> {
        val res = ArrayList(matchResults.keys)
        if (activeMatchWith != null) res += activeMatchWith
        return res
    }

    fun isPlaysNow() = activeMatchWith != null

    fun isFinishedGameWith(otherPlayer: MutablePlayerState) = matchResults.containsKey(otherPlayer)

    /**
     * Количество поражений от игроков из [filteredPlayers] минус количество побед у игроков не из [filteredPlayers].
     */
    // TODO вероятно, вынести в топсорт
    fun getLossesBalance(filteredPlayers: ArrayList<MutablePlayerState>): Int {
        // количество поражений от оставшихся в рассмотрении
        val lossesInSourceCnt = matchResults.values
            .filter { it.otherPlayer in filteredPlayers }
            .count { !it.isWin }

        // количество побед у выбывших из рассмотрения
        val winsNotInSourceCnt = matchResults.values
            .filter { it.otherPlayer !in filteredPlayers }
            .count { it.isWin }

        return lossesInSourceCnt - winsNotInSourceCnt
    }

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
