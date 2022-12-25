import pairSorters.FastSwissPairSorter
import pairSorters.PairSorter
import tableSorters.TableSorter
import tableSorters.TopologicalTableSorter
import java.io.File
import java.io.PrintWriter
import java.util.*

class Tournament(
    private val tablesCnt: Int,
    private val tournamentMatchesPerPlayerCnt: Int,
    private val allPlayers: List<PlayerState>,
    private val tableSorter: TableSorter,
    private val pairSorter: PairSorter,
) {

    private val s = Simulation(allPlayers, tournamentMatchesPerPlayerCnt)

    private var tablesOccupied = 0

    private fun generateNextMatch(): Pair<PlayerState, PlayerState>? {
        val allEligible = allPlayers
            .filter { !it.isPlaysNow() }
            .filter { !it.isPaused }
            .filter { it.matchesPlayed < tournamentMatchesPerPlayerCnt }


        val bestMatch = listAllPairs(allEligible)
            .filter { (player1, player2) -> !player1.isFinishedGameWith(player2) } // проверяем, что не играли раньше

            .sortedBy { (player1, player2) -> pairSorter.assessPair(player1, player2) } // оцениваем пару и сортируем по оценке

            .firstOrNull { (player1, player2) -> // пробуем симулировать до конца
                s.isCorrect(player1 to player2)
            }

        return bestMatch
    }

    fun generateAndStartMatch(writeTo: PrintWriter): Pair<PlayerState, PlayerState>? =
        generateNextMatch()?.also { startMatch(it) }?.also { writeTo.println("$GO_TO_TABLE_PREFIX${it.first.name} ${it.second.name}") }


    private fun startMatch(p: Pair<PlayerState, PlayerState>) {
        tablesOccupied++
        p.first.startMatchWith(p.second)
        p.second.startMatchWith(p.first)
        s.play(p)
    }

    private fun endMatch(p: Pair<PlayerState, PlayerState>, sets: Pair<Int, Int>) {
        tablesOccupied--
        p.first.endMatch(sets.first, sets.second)
        p.second.endMatch(sets.second, sets.first)
    }

    private fun parseMatchLine(allPlayers: ArrayList<PlayerState>, line: String) {
        val tok = line.split(" ")

        val player1 = allPlayers.first { it.name == tok[0] }
        val player2 = allPlayers.first { it.name == tok[1] }

        // начинаем матч
        startMatch(player1 to player2)

        when (tok.size) {

            2 -> { // незавершённый матч
                return
            }

            4 -> { // результаты матча
                val sets1 = line.split(" ")[2].toInt()
                val sets2 = line.split(" ")[3].toInt()
                endMatch(player1 to player2, sets1 to sets2)
            }

            else -> throw IllegalArgumentException("Неверный формат строки: '$line'")

        }
    }

    fun hasFreeTables() = tablesOccupied < tablesCnt


    fun outputCurrentTable() {
        val allPlayersSorted = tableSorter.sorted(allPlayers)
//            allPlayers.sortedWith(ScoreAndBergerScoreSorter())

        val maxNameLength = allPlayers.maxOf { it.name.length } + 2
        print(
            "Место ".padEnd(6)
                    + "Ранг "
                    + "Игрок".padEnd(maxNameLength)
                    + "Игр".padEnd(5)
                    + "Побед".padEnd(11)
                    + "Б-Побед".padEnd(11)
                    + "Сетов".padEnd(13)
                    + "Б-Сетов".padEnd(13)
        )
        repeat(allPlayersSorted.size) { idx -> print(" ${idx + 1}".padEnd(5)) }
        println()

        for ((index, player) in allPlayersSorted.withIndex()) {
            print(
                ("" + (index + 1) + ". ").padStart(6)
                        + ("(${player.topSortRank})").padStart(4) + " "
                        + player.name.padEnd(maxNameLength)
                        + (player.matchesPlayed.toString() + if (player.isPlaysNow()) "*" else "").padEnd(5)
                        + (player.score.wins.toString() + " (%.2f)".format(player.score.winsAvg)).padEnd(11)
                        + (player.bergerScore.wins.toString() + " (%.2f)".format(player.bergerScore.winsAvg)).padEnd(11)
                        + ("%+d".format(player.score.setsDiff) + " (%+.2f)".format(player.score.setsDiffAvg)).padEnd(13)
                        + ("%+d".format(player.bergerScore.setsDiff) + " (%+.2f)".format(player.bergerScore.setsDiffAvg)).padEnd(13)
            )

            for ((otherIndex, otherPlayer) in allPlayersSorted.withIndex()) {
                val match = player.matchResults[otherPlayer]
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
//Топсорт:
//
//1. Митрохин
//2. Ровда
//3. Молчанов
//4. Павлов
//5. Шалыгин
//6-7. Попович, Уни
//8. Мелёхин
//9, 10. Шелкович, Оленников
//11. Власов
//12, 13. Макаровский, Шаймарданов
//14. Швалёв
//15. Куприянов
//16. Нестеров
//17. Попов
//18. ЛУЗЕР

    companion object {

        private const val GO_TO_TABLE_PREFIX = "К СТОЛУ --> "

        private val TABLE_SORTER: TableSorter = TopologicalTableSorter()

        // TODO сделать сортировку по топсорту. Вот критерии:
        //  1. Несравнимые лучше сравнимых. Даже независимо от ранга.
        //	 Несравнимые — значит с учётом транзитивности их результат между собой неясен.
        //	 Более формально: между игроками нет пути по направленным рёбрам (направление — от победителя к проигравшему в матче).
        //  2. Минимизация текущей разницы рангов
        private val PAIR_SORTER: PairSorter = FastSwissPairSorter()

        fun parse(inputFile: File, copyTo: PrintWriter): Tournament {
            var tablesCnt = 1
            var tournamentMatchesPerPlayerCnt = 1
            var handicapToursCnt = 0
            val allPlayers = ArrayList<PlayerState>()

            var t: Tournament? = null

            val sc = Scanner(inputFile)
            while (sc.hasNextLine()) {
                val line = sc.nextLine().removePrefix(GO_TO_TABLE_PREFIX)
                copyTo.println(line)

                val lineTrimmed = line.trim()
                if (lineTrimmed.isBlank()) { // пропускаем пустые строки
                    continue
                } else if (lineTrimmed.startsWith("#")) { // пропускаем комменты
                    continue
                } else if (lineTrimmed.lowercase().startsWith("Стол".lowercase())) { // Столов
                    tablesCnt = lineTrimmed.split(" ").last().toInt()
                } else if (lineTrimmed.lowercase().startsWith("Матч".lowercase())) { // Матчей
                    tournamentMatchesPerPlayerCnt = lineTrimmed.split(" ").last().toInt()
                } else if (lineTrimmed.lowercase().startsWith("Гандикап".lowercase())) { // ГандикапИгр
                    handicapToursCnt = lineTrimmed.split(" ").last().toInt()
                } else if (lineTrimmed.lowercase().startsWith("Игрок".lowercase())) { // Игрок
                    val tok = lineTrimmed.split(" ")
                    val name = tok[1].removePrefix("-")
                    val isPaused = tok[1].startsWith("-")
                    val handicapWins = tok.getOrNull(2)?.toInt() ?: 0
                    val handicapLosses = tok.getOrNull(3)?.toInt() ?: 0
                    allPlayers += PlayerState(name, isPaused, handicapToursCnt, handicapWins, handicapLosses)
                } else if (lineTrimmed.split(" ").first().let { name -> allPlayers.count { it.name == name } > 0 }) { // Результат матча
                    // если игрок уже есть в списке, то просто добавляем результаты
                    if (t == null) t = Tournament(tablesCnt, tournamentMatchesPerPlayerCnt, allPlayers, TABLE_SORTER, PAIR_SORTER)
                    t.parseMatchLine(allPlayers, lineTrimmed)
                } else {
                    throw IllegalArgumentException("Не могу разобрать строку: '$lineTrimmed'")
                }
            }
            sc.close()

            return t ?: Tournament(tablesCnt, tournamentMatchesPerPlayerCnt, allPlayers, TABLE_SORTER, PAIR_SORTER)
        }

        private fun listAllPairs(curEligible: List<PlayerState>) = curEligible
            .flatMapIndexed { i1, player1 ->
                curEligible
                    .filterIndexed { i2, _ -> i2 > i1 }
                    .map { player2 -> player1 to player2 }
            }

    }

}
