package fastSwiss.api

/**
 * Игрок, включая его статистику на турнире.
 */
// TODO добавить больше защиты от неправильных действий, так как в Телеграм-Боте это уже актуально.
class MutablePlayerState(
    val name: String,
    val isPaused: Boolean,
    private val handicapTours: Int,
    private val handicapWins: Int,
    private val handicapLosses: Int,
) {

    private var activeMatchWith: MutablePlayerState? = null // игрок, с которым сейчас идёт матч
    val matchResults = HashMap<MutablePlayerState, PlayerMatchResult>()

    val matchesPlayed by lazy { matchResults.size }

    // TODO поддержать везде ничьи
    private val matchesWonCnt by lazy { matchResults.values.sumOf { it.winsMy } }
    private val drawsCnt by lazy { matchResults.values.sumOf { it.drawsMy } }
    private val setsDiff by lazy { matchResults.values.sumOf { it.setsDiff } }

    // TODO вынуть из этого класса элементы tableSorters и тому подобное
    var topSortRank: Int? = null

    // TODO вынести в pairSorter
    val score by lazy {
        Score(
            matchesPlayed,
            matchesWonCnt,
            setsDiff,
            if (matchesPlayed < handicapTours) handicapWins else 0,
            if (matchesPlayed < handicapTours) handicapLosses else 0,
        )
    }

    // TODO вынести в pairSorter
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
