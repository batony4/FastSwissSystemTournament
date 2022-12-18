import kotlin.math.abs

class Player(
    val name: String,
) : Comparable<Player> {

    var activeMatchWith: String? = null // игрок, с которым сейчас идёт матч
    val matchResults = HashMap<String, PlayerMatchResult>()

    fun getMatchesPlayed() = matchResults.size
    fun getMatchesWon() = matchResults.values.sumOf { it.winsMy }
    fun getSetsDiff() = matchResults.values.sumOf { it.setsMy - it.setsOther }

    // TODO реализовать коэффициент Бергера в качестве третьего параметра, и добавить также в сравнение
    // TODO деление на ноль. в случае 0 матчей проставлять средние значения
    fun getAveragePointsPerMatch() = (getMatchesWon().toDouble() / getMatchesPlayed()) to (getSetsDiff().toDouble() / getMatchesPlayed())

    fun isPlaysNow() = activeMatchWith != null

    fun startMatchWith(otherPlayer: String) {
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

    override fun compareTo(other: Player): Int {
        val myPoints = getAveragePointsPerMatch()
        val otherPoints = other.getAveragePointsPerMatch()

        if (abs(myPoints.first - otherPoints.first) < 1e-9) {
            return myPoints.first.compareTo(otherPoints.first)
        }
        if (abs(myPoints.second - otherPoints.second) < 1e-9) {
            return myPoints.second.compareTo(otherPoints.second)
        }
        return 0
    }

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
