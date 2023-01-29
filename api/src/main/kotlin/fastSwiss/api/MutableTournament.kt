package fastSwiss.api

import fastSwiss.api.pairSorters.PairSorter
import fastSwiss.api.pairSorters.TopologicalPairSorter
import fastSwiss.api.tableSorters.TableSorter
import fastSwiss.api.tableSorters.TopologicalTableSorter
import java.io.File
import java.io.PrintWriter
import java.util.*
import kotlin.jvm.Throws
import kotlin.math.max

// TODO сделать его mutable, чтобы можно было апдейтить на ходу
// TODO вынести отсюда методы, связанные с представлением. Здесь должна быть только модель
// TODO добавить больше защиты от неправильных действий, так как в Телеграм-Боте это уже актуально.
class MutableTournament(
    private val tablesCnt: Int,
    private val tournamentMatchesPerPlayerCnt: Int,
    private val allPlayers: List<MutablePlayerState>,
    private val tableSorter: TableSorter,
    private val pairSorter: PairSorter,
) {

    private val s = Simulation(allPlayers, tournamentMatchesPerPlayerCnt)

    private var tablesOccupied = 0

    private fun generateNextMatch(): Pair<MutablePlayerState, MutablePlayerState>? {
        val allEligible = allPlayers
            .filter { !it.isPlaysNow() }
            .filter { !it.isPaused }
            .filter { it.matchesPlayed < tournamentMatchesPerPlayerCnt }


        val bestMatch = listAllPairs(allEligible)
            .filter { (player1, player2) -> !player1.isFinishedGameWith(player2) } // проверяем, что не играли раньше
            .sortedBy { (player1, player2) -> pairSorter.assessPair(player1, player2, allPlayers) } // оцениваем пару и сортируем по оценке
            .firstOrNull { (player1, player2) -> // пробуем симулировать до конца
                s.isCorrect(player1 to player2)
            }

        return bestMatch
    }

    fun generateAndStartMatch(writeTo: PrintWriter): Pair<MutablePlayerState, MutablePlayerState>? =
        generateNextMatch()?.also { startMatch(it) }?.also { writeTo.println("$GO_TO_TABLE_PREFIX${it.first.name} ${it.second.name}") }


    /**
     * Добавление нового игрока.
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     * Проверка на корректность актуальна: например, может быть такая ситуация, что новому игроку надо сыграть много матчей, а все
     * остальные уже доиграли все свои матчи.
     */
    @Throws(IncorrectChangeException::class)
    fun addPlayer(player: MutablePlayerState, check: Boolean) {
        TODO()
    }

    /**
     * Удаление игрока.
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     * Проверка на корректность актуальна: например, нельзя удалить игрока, который сыграл хотя бы один матч.
     * Также может быть такое, что с учётом этого игрока все пары сходились (даже если он пока не сыграл ни одного матча), а после его
     * удаления сходиться перестали.
     */
    @Throws(IncorrectChangeException::class)
    fun removePlayer(player: MutablePlayerState, check: Boolean) {
        TODO()
    }

    /**
     * Изменение количества матчей, которые должен сыграть каждый игрок.
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     * Проверка на корректность актуальна: например, при нельзя сократить количество матчей до N, если хотя бы один игрок уже сыграл
     * более чем N матчей.
     * Также может быть такое, что все пары сходились, а при уменьшении количества оставшихся матчей — перестали.
     */
    @Throws(IncorrectChangeException::class)
    fun changeTournamentMatchesPerPlayerCnt(newTournamentMatchesPerPlayerCnt: Int, check: Boolean) {
        TODO()
    }

    private fun startMatch(p: Pair<MutablePlayerState, MutablePlayerState>) {
        tablesOccupied++
        p.first.startMatchWith(p.second)
        p.second.startMatchWith(p.first)
        s.play(p)
    }

    private fun endMatch(p: Pair<MutablePlayerState, MutablePlayerState>, sets: Pair<Int, Int>) {
        tablesOccupied--
        p.first.endMatch(sets.first, sets.second)
        p.second.endMatch(sets.second, sets.first)
    }

    // TODO весь парсинг вынести в модуль fileInterface
    private fun parseMatchLine(allPlayers: ArrayList<MutablePlayerState>, line: String) {
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

    fun calcCurrentTable() = tableSorter.sorted(allPlayers)

    fun outputCurrentTable(allPlayersSorted: List<MutablePlayerState>) {
        val maxNameLength = allPlayers.maxOf { it.name.length } + 2
        print(
            "Место ".padEnd(6)
                    + "Ранг "
                    + "Игрок".padEnd(maxNameLength) + " "
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
                        + player.name.padEnd(max(maxNameLength, "Игрок".length)) + " "
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
                    val delimiter = if ((index > otherIndex) && (match.setsMy > match.setsOther)) {
                        "↑"
                    } else if ((index < otherIndex) && (match.setsMy < match.setsOther)) {
                        "↓"
                    } else {
                        ":"
                    }

                    print("${match.setsMy}$delimiter${match.setsOther}".padEnd(5))
                } else {
                    print(" •".padEnd(5))
                }
            }
            println()
        }
    }

    companion object {

        private const val GO_TO_TABLE_PREFIX = "К СТОЛУ --> "

        private val TABLE_SORTER: TableSorter = TopologicalTableSorter()
        private val PAIR_SORTER: PairSorter = TopologicalPairSorter()

        fun parse(inputFile: File, copyTo: PrintWriter): MutableTournament {
            var tablesCnt = 1
            var tournamentMatchesPerPlayerCnt = 1
            var handicapToursCnt = 0
            val allPlayers = ArrayList<MutablePlayerState>()

            var t: MutableTournament? = null

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
                    allPlayers += MutablePlayerState(name, isPaused, handicapToursCnt, handicapWins, handicapLosses)
                } else if (lineTrimmed.split(" ").first().let { name -> allPlayers.count { it.name == name } > 0 }) { // Результат матча
                    // если игрок уже есть в списке, то просто добавляем результаты
                    if (t == null) t = MutableTournament(tablesCnt, tournamentMatchesPerPlayerCnt, allPlayers, TABLE_SORTER, PAIR_SORTER)
                    t.parseMatchLine(allPlayers, lineTrimmed)
                } else {
                    throw IllegalArgumentException("Не могу разобрать строку: '$lineTrimmed'")
                }
            }
            sc.close()

            return t ?: MutableTournament(tablesCnt, tournamentMatchesPerPlayerCnt, allPlayers, TABLE_SORTER, PAIR_SORTER)
        }

        private fun listAllPairs(curEligible: List<MutablePlayerState>) = curEligible
            .flatMapIndexed { i1, player1 ->
                curEligible
                    .filterIndexed { i2, _ -> i2 > i1 }
                    .map { player2 -> player1 to player2 }
            }

    }

}