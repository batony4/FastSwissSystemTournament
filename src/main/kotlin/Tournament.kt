import kotlin.math.abs

class Tournament {

    // конфиг
    private var tablesCnt = 1
    private var tournamentMatchesPerPlayerCnt = 1
    private val allPlayers = ArrayList<Player>()

    private var tablesOccupied = 0

    private val s by lazy { Simulation(allPlayers, tournamentMatchesPerPlayerCnt) }

    fun getAllPlayers(): List<Player> = allPlayers

    private fun createAllPairs(curEligible: List<Player>) = curEligible
        .flatMapIndexed { i1, player1 ->
            curEligible
                .filterIndexed { i2, _ -> i2 > i1 }
                .map { player2 -> player1 to player2 }
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
                    s.isPossibleToCreateCorrectTournamentStructureUntilTheEnd(player1, player2)
                }

            if (bestMatch != null) {
                return bestMatch
            }
        }

        return null
    }

    fun generateAndStartMatch(): Pair<Player, Player>? = generateNextMatch()?.also { startMatch(it) }

    private fun startMatch(match: Pair<Player, Player>) {
        match.first.startMatchWith(match.second)
        match.second.startMatchWith(match.first)
    }

    private fun endMatch(match: Pair<Player, Player>, sets: Pair<Int, Int>) {
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
            throw IllegalArgumentException("Неверный формат строки: '$line'")
        }
    }

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

    fun hasFreeTables() = tablesOccupied < tablesCnt

}
