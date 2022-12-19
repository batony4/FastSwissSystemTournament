class Simulation(
    private val allPlayers: List<Player>,
    private val tournamentMatchesPerPlayerCnt: Int,
) {
    private val m = Array(allPlayers.size) { BooleanArray(allPlayers.size) } // матрица, кто с кем играл (j > i).
    private val cnt = Array(allPlayers.size) { 0 } // сколько матчей сыграл i-ый игрок

    private val allPlayersSorted = allPlayers.sorted()

    init {
        allPlayersSorted.forEachIndexed { i1, p1 ->
            p1.matchResults.forEach { (_, result) ->
                val i2 = allPlayersSorted.indexOf(result.otherPlayer)
                if (i2 > i1) play(i1, i2)
            }

            p1.activeMatchWith?.let { p2 ->
                val i2 = allPlayersSorted.indexOf(p2)
                if (i2 > i1) play(i1, i2)
            }
        }
    }

    fun play(p: Pair<Player, Player>) {
        val player1Index = allPlayersSorted.indexOf(p.first)
        val player2Index = allPlayersSorted.indexOf(p.second)
        if (player1Index > player2Index) throw IllegalArgumentException("player1Index должен быть меньше player2Index")

        play(player1Index, player2Index)
    }

    private fun play(i1: Int, i2: Int) {
        m[i1][i2] = true
        cnt[i1]++
        cnt[i2]++
    }

    private fun unplay(i1: Int, i2: Int) {
        m[i1][i2] = false
        cnt[i1]--
        cnt[i2]--
    }

    /**
     * Пытаемся симулировать, получится ли полностью составить план матчей с учётом этого матча.
     */
    private fun rec(): Boolean {
        if (cnt.all { it == tournamentMatchesPerPlayerCnt }) return true

        for (i in m.indices.shuffled().sortedBy { cnt[it] }) {
            if (cnt[i] >= tournamentMatchesPerPlayerCnt) continue

            for (j in (i + 1 until m.size).shuffled().sortedBy { cnt[it] }) {
                if (cnt[j] >= tournamentMatchesPerPlayerCnt) continue

                if (!m[i][j]) {
                    play( i, j)
                    if (rec().also { unplay(i, j) }) return true
                }
            }
        }

        return false
    }

    /**
     * Проверяет, существует ли какой-то набор матчей, который позволит создать корректную структуру турнира до самого конца при условии,
     * что игроки player1 и player2 будут играть в ближайшем матче.
     */
    fun isPossibleToCreateCorrectTournamentStructureUntilTheEnd(
        player1: Player,
        player2: Player,
    ): Boolean {
        val player1Index = allPlayersSorted.indexOf(player1)
        val player2Index = allPlayersSorted.indexOf(player2)

        play(player1Index, player2Index)
        return rec().also { unplay(player1Index, player2Index) }
    }

}
