class Player(
    val name: String,
) : Comparable<Player> {

    var activeMatchWith: Player? = null // игрок, с которым сейчас идёт матч
    val matchResults = HashMap<String, PlayerMatchResult>()

    val matchesPlayed by lazy { matchResults.size }
    val matchesWon by lazy { matchResults.values.sumOf { it.winsMy } }
    val setsDiff by lazy { matchResults.values.sumOf { it.setsMy - it.setsOther } }

    val score by lazy {
        Score(
            matchesPlayed = matchesPlayed,
            wins = matchesWon,
            setsDiff = setsDiff,

            // TODO реализовать коэффициент Бергера
//            matchResults.values.sumOf { it. }0,
            0,
            0,
            0,
        )
    }

    fun isPlaysNow() = activeMatchWith != null

    fun startMatchWith(otherPlayer: Player) {
        if (activeMatchWith != null) {
            throw IllegalStateException("Уже играем с $activeMatchWith")
        }
        activeMatchWith = otherPlayer
    }

    fun endMatch(setsMy: Int, setsOther: Int) {
        activeMatchWith?.let { otherPlayer ->
            matchResults[otherPlayer.name] = PlayerMatchResult(otherPlayer, setsMy, setsOther)
            activeMatchWith = null
        } ?: throw IllegalStateException("Не играем сейчас")
    }

    override fun compareTo(other: Player) = score.compareTo(other.score)

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Player) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}
