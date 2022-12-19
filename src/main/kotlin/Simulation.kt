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
                if (i2 > i1) play(m, cnt, i1, i2)
            }

            p1.activeMatchWith?.let { p2 ->
                val i2 = allPlayersSorted.indexOf(p2)
                if (i2 > i1) play(m, cnt, i1, i2)
            }
        }
    }

    private fun play(m: Array<BooleanArray>, cnt: Array<Int>, i1: Int, i2: Int) {
        m[i1][i2] = true
        cnt[i1]++
        cnt[i2]++
    }

    private fun unplay(m: Array<BooleanArray>, cnt: Array<Int>, i1: Int, i2: Int) {
        m[i1][i2] = false
        cnt[i1]--
        cnt[i2]--
    }

    /**
     * Пытаемся симулировать, получится ли полностью составить план матчей с учётом этого матча.
     */
    private fun rec(m: Array<BooleanArray>, cnt: Array<Int>, tournamentMatchesPerPlayerCnt: Int): Boolean {
        if (cnt.all { it == tournamentMatchesPerPlayerCnt }) return true

        for (i in m.indices.shuffled().sortedBy { cnt[it] }) {
            if (cnt[i] >= tournamentMatchesPerPlayerCnt) continue

            for (j in (i + 1 until m.size).shuffled().sortedBy { cnt[it] }) {
                if (cnt[j] >= tournamentMatchesPerPlayerCnt) continue

                if (!m[i][j]) {
                    play(m, cnt, i, j)
                    if (rec(m, cnt, tournamentMatchesPerPlayerCnt).also { unplay(m, cnt, i, j) }) return true
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

        play(m, cnt, player1Index, player2Index)
        return rec(m, cnt, tournamentMatchesPerPlayerCnt).also { unplay(m, cnt, player1Index, player2Index) }
    }

}
