package fastSwiss.api

import kotlin.math.max
import kotlin.math.min

/**
 * Симуляция возможности расстановки пар до конца турнира с учётом уже сыгранных пар и предлагаемой следующей пары.
 */
class Simulation(
    private val allPlayers: List<MutablePlayerState>,
    private val tournamentMatchesPerPlayerCnt: Int,
) {
    private val m = Array(allPlayers.size) { BooleanArray(allPlayers.size) } // матрица, кто с кем играл (j > i).
    private val cnt = Array(allPlayers.size) { 0 } // сколько матчей сыграл i-ый игрок

    private fun play(i1: Int, i2: Int) {
        if (m[i1][i2]) throw IllegalArgumentException("Уже сыграли $i1 и $i2")
        if (i1 >= i2) throw IllegalArgumentException("Неверный порядок параметров. Требуется: $i1 < $i2")

        m[i1][i2] = true
        cnt[i1]++
        cnt[i2]++
    }

    private fun unplay(i1: Int, i2: Int) {
        if (!m[i1][i2]) throw IllegalArgumentException("Не сыграли $i1 и $i2")
        if (i1 >= i2) throw IllegalArgumentException("Неверный порядок параметров. Требуется: $i1 < $i2")

        m[i1][i2] = false
        cnt[i1]--
        cnt[i2]--
    }

    fun play(p: Pair<MutablePlayerState, MutablePlayerState>) {
        val i1 = allPlayers.indexOf(p.first)
        val i2 = allPlayers.indexOf(p.second)

        play(min(i1, i2), max(i1, i2))
    }

    /**
     * Пытаемся симулировать, получится ли полностью составить план матчей из текущей ситуации.
     */
    // TODO симуляция, можно ли поставить пару, иногда работает ужасно долго
    private fun isCorrect(): Boolean {
        if (cnt.all { it >= tournamentMatchesPerPlayerCnt }) return true

        // в случае нечётного количества игроков, допускаем ситуацию, когда один игрок не сыграет один матч
        if ((allPlayers.size % 2 == 1)
            && (cnt.count { it == tournamentMatchesPerPlayerCnt - 1 } == 1)
            && (cnt.count { it == tournamentMatchesPerPlayerCnt } == allPlayers.size - 1)
        )
            return true

        for (i in m.indices.shuffled().sortedBy { cnt[it] }) {
            if (cnt[i] >= tournamentMatchesPerPlayerCnt) continue

            for (j in (i + 1 until m.size).shuffled().sortedBy { cnt[it] }) {
                if (cnt[j] >= tournamentMatchesPerPlayerCnt) continue

                if (!m[i][j]) {
                    play(i, j)
                    if (isCorrect().also { unplay(i, j) }) return true
                }
            }
        }

        return false
    }

    /**
     * Удастся ли составить корректный план игр, если начать с матча [p]?
     */
    fun isCorrect(p: Pair<MutablePlayerState, MutablePlayerState>): Boolean {
        val i1 = allPlayers.indexOf(p.first)
        val i2 = allPlayers.indexOf(p.second)

        play(min(i1, i2), max(i1, i2))
        return isCorrect().also { unplay(min(i1, i2), max(i1, i2)) }
    }

}
