import kotlin.math.abs

class Tournament {

    // конфиг
    private var tablesCnt = 1
    private var tournamentMatchesPerPlayerCnt = 1
    private var handicapToursCnt = 0
    private val allPlayers = ArrayList<Player>()

    private var tablesOccupied = 0

    private val s by lazy { Simulation(allPlayers, tournamentMatchesPerPlayerCnt) }

    private fun generateNextMatch(): Pair<Player, Player>? {
        val allPlayersSorted = allPlayers.sorted()
        val allEligible = allPlayersSorted
            .filter { !it.isPlaysNow() }
            .filter { it.matchesPlayed < tournamentMatchesPerPlayerCnt }

        for (maxMatchesPlayed in 0 until tournamentMatchesPerPlayerCnt) {
            val curEligible = allEligible.filter { it.matchesPlayed <= maxMatchesPlayed }

            val bestMatch = createAllPairs(curEligible)
                .filter { (player1, player2) -> !player1.isPlayedWith(player2) } // проверяем, что не играли раньше

                // сортируем по близости игроков между собой с учётом невидимого гандикапа
                .sortedBy { (player1, player2) -> abs(player1.score.winsAvgWithHandicap - player2.score.winsAvgWithHandicap) }

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
        tablesOccupied++
        match.first.startMatchWith(match.second)
        match.second.startMatchWith(match.first)
    }

    private fun endMatch(match: Pair<Player, Player>, sets: Pair<Int, Int>) {
        tablesOccupied--
        match.first.endMatch(sets.first, sets.second)
        match.second.endMatch(sets.second, sets.first)
    }

    private fun parseMatchLine(allPlayers: ArrayList<Player>, line: String) {
        val tok = line.split(" ")

        val player1 = allPlayers.first { it.name == tok[0] }
        val player2 = allPlayers.first { it.name == tok[1] }

        // начинаем матч
        startMatch(player1 to player2)

        if (tok.size == 2) { // незавершённый матч
            return
        } else if (tok.size == 4) { // результаты матча
            val sets1 = line.split(" ")[2].toInt()
            val sets2 = line.split(" ")[3].toInt()
            endMatch(player1 to player2, sets1 to sets2)
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
            parseMatchLine(allPlayers, lineTrimmed)
        } else if (lineTrimmed.lowercase().startsWith("Стол".lowercase())) {
            tablesCnt = lineTrimmed.split(" ").last().toInt()
        } else if (lineTrimmed.lowercase().startsWith("Матч".lowercase())) {
            tournamentMatchesPerPlayerCnt = lineTrimmed.split(" ").last().toInt()
        } else if (lineTrimmed.lowercase().startsWith("Гандикап".lowercase())) {
            handicapToursCnt = lineTrimmed.split(" ").last().toInt()
        } else if (lineTrimmed.lowercase().startsWith("Игрок".lowercase())) {
            val tok = lineTrimmed.split(" ")
            val name = tok[1]
            val handicapWins = tok.getOrNull(2)?.toInt() ?: 0
            val handicapLosses = tok.getOrNull(3)?.toInt() ?: 0
            allPlayers += Player(name, handicapToursCnt, handicapWins, handicapLosses)
        } else {
            throw IllegalArgumentException("Не могу разобрать строку: '$lineTrimmed'")
        }
    }

    fun hasFreeTables() = tablesOccupied < tablesCnt


    fun outputCurrentTable() {
        val allPlayersSorted = allPlayers.sorted()
        val maxNameLength = allPlayers.maxOf { it.name.length } + 2
        print(
            "Место ".padEnd(6)
                    + "Игрок".padEnd(maxNameLength)
                    + "Игр".padEnd(5)
                    + "Побед".padEnd(11)
                    + "Сетов".padEnd(13)
                    + "Б-Побед".padEnd(11)
                    + "Б-Сетов".padEnd(13)
        )
        repeat(allPlayersSorted.size) { idx -> print(" ${idx + 1}".padEnd(5)) }
        println()

        for ((index, player) in allPlayersSorted.withIndex()) {
            print(
                ("" + (index + 1) + ". ").padStart(6)
                        + player.name.padEnd(maxNameLength)
                        + (player.matchesPlayed.toString() + if (player.isPlaysNow()) "*" else "").padEnd(5)
                        + (player.score.wins.toString() + " (%.2f)".format(player.score.winsAvg)).padEnd(11)
                        + ("%+d".format(player.score.setsDiff) + " (%+.2f)".format(player.score.setsDiffAvg)).padEnd(13)
                        + (player.bergerScore.wins.toString() + " (%.2f)".format(player.bergerScore.winsAvg)).padEnd(11)
                        + ("%+d".format(player.bergerScore.setsDiff) + " (%+.2f)".format(player.bergerScore.setsDiffAvg)).padEnd(13)
            )

            for ((otherIndex, otherPlayer) in allPlayersSorted.withIndex()) {
                val match = player.matchResults[otherPlayer.name]
                if (index == otherIndex) {
                    print(" X   ")
                } else if (match != null) {
                    print("${match.setsMy}:${match.setsOther}".padEnd(5))
                } else {
                    print(" •".padEnd(5))
                }
            }
            println()
        }
    }

    companion object {

        private fun createAllPairs(curEligible: List<Player>) = curEligible
            .flatMapIndexed { i1, player1 ->
                curEligible
                    .filterIndexed { i2, _ -> i2 > i1 }
                    .map { player2 -> player1 to player2 }
            }

    }

}
