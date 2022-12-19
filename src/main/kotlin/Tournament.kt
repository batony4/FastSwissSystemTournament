import kotlin.math.abs

class Tournament {

    // конфиг
    private var tablesCnt = 1
    private var tournamentMatchesPerPlayerCnt = 1
    private val allPlayers = ArrayList<Player>()

    private var tablesOccupied = 0

    fun getAllPlayers(): List<Player> = allPlayers

    fun parseLine(line: String) {
        val lineTrimmed = line.trim()

        if (lineTrimmed.isBlank()) { // пропускаем пустые строки
            return
        } else if (lineTrimmed.startsWith("#")) { // пропускаем комменты
            return
        } else if (lineTrimmed.split(" ").first().let { name -> allPlayers.count { it.name == name } > 0 }) {
            // если игрок уже есть в списке, то просто добавляем результаты
            if (parseMatchLine(allPlayers, lineTrimmed)) tablesOccupied++
        } else if (lineTrimmed.lowercase().startsWith("Стол".lowercase())) {
            tablesCnt = lineTrimmed.split(" ").last().toInt()
        } else if (lineTrimmed.lowercase().startsWith("Матч".lowercase())) {
            tournamentMatchesPerPlayerCnt = lineTrimmed.split(" ").last().toInt()
        } else if (lineTrimmed.lowercase().startsWith("Игрок".lowercase())) {
            allPlayers += Player(lineTrimmed.split(" ").last())
        } else {
            throw IllegalArgumentException("Не могу разобрать строку: '$lineTrimmed'")
        }
    }

    private fun createAllPairs(curEligible: List<Player>) = curEligible
        .flatMapIndexed { i1, player1 ->
            curEligible
                .filterIndexed { i2, _ -> i2 > i1 }
                .map { player2 -> player1 to player2 }
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
    fun rec(m: Array<BooleanArray>, cnt: Array<Int>, tournamentMatchesPerPlayerCnt: Int): Boolean {
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
    // TODO вынести эту симуляцию в отдельный класс
    private fun isPossibleToCreateCorrectTournamentStructureUntilTheEnd(
        player1: Player,
        player2: Player,
    ): Boolean {
        val allPlayersSorted = allPlayers.sorted()
        val m = Array(allPlayersSorted.size) { BooleanArray(allPlayersSorted.size) } // матрица, кто с кем играл (j > i).
        val cnt = Array(allPlayersSorted.size) { 0 }

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

        val player1Index = allPlayersSorted.indexOf(player1)
        val player2Index = allPlayersSorted.indexOf(player2)

        play(m, cnt, player1Index, player2Index)
        return rec(m, cnt, tournamentMatchesPerPlayerCnt).also { unplay(m, cnt, player1Index, player2Index) }
    }

    private fun generateNextMatch(): Pair<Player, Player>? {
        val allPlayersSorted = allPlayers.sorted()
        val allEligible = allPlayersSorted
            .filter { !it.isPlaysNow() }
            .filter { it.matchesPlayed < tournamentMatchesPerPlayerCnt }

        for (maxMatchesPlayed in 0 until tournamentMatchesPerPlayerCnt) {
            val curEligible = allEligible.filter { it.matchesPlayed <= maxMatchesPlayed }

            val bestMatch = createAllPairs(curEligible)
                .filter { (player1, player2) -> !player1.isPlayedWith(player2) } // проверяем, что не играли раньше
                .sortedBy { (player1, player2) -> abs(player1.score.winsAvg - player2.score.winsAvg) } // сортируем по близости игроков между собой
                .firstOrNull { (player1, player2) -> // пробуем симулировать до конца
                    isPossibleToCreateCorrectTournamentStructureUntilTheEnd(player1, player2)
                }

            if (bestMatch != null) {
                return bestMatch
            }
        }

        return null
    }

    fun generateAndStartMatch(): Pair<Player, Player>? = generateNextMatch()?.also { startMatch(it) }

    fun startMatch(match: Pair<Player, Player>) {
        match.first.startMatchWith(match.second)
        match.second.startMatchWith(match.first)
    }

    fun endMatch(match: Pair<Player, Player>, sets: Pair<Int, Int>) {
        match.first.endMatch(sets.first, sets.second)
        match.second.endMatch(sets.second, sets.first)
    }

    /**
     * Возвращает, был ли начат новый незавершённый матч.
     */
    private fun parseMatchLine(allPlayers: ArrayList<Player>, line: String): Boolean {
        val tok = line.split(" ")

        val player1 = allPlayers.first { it.name == tok[0] }
        val player2 = allPlayers.first { it.name == tok[1] }

        // начинаем матч
        startMatch(player1 to player2)

        if (tok.size == 2) { // незавершённый матч
            return true
        } else if (tok.size == 4) { // результаты матча
            val sets1 = line.split(" ")[2].toInt()
            val sets2 = line.split(" ")[3].toInt()
            endMatch(player1 to player2, sets1 to sets2)
            return false
        } else {
            throw Exception("Неверный формат строки: '$line'")
        }
    }

    fun hasFreeTables() = tablesOccupied < tablesCnt

}
