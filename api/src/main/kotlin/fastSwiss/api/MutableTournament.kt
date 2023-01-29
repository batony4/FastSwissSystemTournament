package fastSwiss.api

import fastSwiss.api.tournamentTypes.PairSorter
import fastSwiss.api.tournamentTypes.Ranker
import fastSwiss.api.tournamentTypes.Ranking
import java.util.*

class MutableTournament<R : Ranking>(
    private val ranker: Ranker<R>,
    private val pairSorter: PairSorter<R>,
) {

    // Настройки турнира

    private var tablesCnt: Int = 1
    private var tournamentMatchesPerPlayerCnt: Int = 1
    private var allPlayers = ArrayList<MutablePlayerState>()

    // Текущее состояние турнира

    private var tablesOccupied = 0

    // Внутренние методы

    private fun generateNextMatch(): Pair<MutablePlayerState, MutablePlayerState>? {
        val s = Simulation(allPlayers, tournamentMatchesPerPlayerCnt)
        val r = ranker.generate(allPlayers)

        val allEligible = allPlayers
            .filter { !it.isPlaysNow() }
            .filter { !it.isPaused }
            .filter { it.matchesFinishedCnt < tournamentMatchesPerPlayerCnt }


        val bestMatch = listAllPairs(allEligible)
            .filter { (player1, player2) -> !player1.isFinishedGameWith(player2) } // проверяем, что не играли раньше
            .sortedBy { (player1, player2) -> pairSorter.assessPair(player1, player2, r) } // оцениваем пару и сортируем по оценке
            .firstOrNull { (player1, player2) -> // пробуем симулировать до конца
                s.isCorrectWithMatch(player1 to player2)
            }

        return bestMatch
    }

    // ----- API -----

    fun findPlayerByName(name: String): MutablePlayerState? = allPlayers.firstOrNull { it.name == name }

    // TODO реализовать опциональную проверку ([check]).
    //  что с этим матчем сходится турнир,
    //  что они ещё не играли друг с другом,
    //  что оба игрока в данный момент свободны,
    //  что игроки существуют
    fun startMatch(p: Pair<String, String>) {
        val p1 = findPlayerByName(p.first)!!
        val p2 = findPlayerByName(p.second)!!

        tablesOccupied++
        p1.startMatchWith(p2)
        p2.startMatchWith(p1)
    }

    // TODO реализовать опциональную проверку ([check]).
    //  что они действительно играли именно друг с другом,
    //  что игроки существуют
    fun endMatch(p: Pair<String, String>, sets: Pair<Int, Int>) {
        val p1 = findPlayerByName(p.first)!!
        val p2 = findPlayerByName(p.second)!!

        tablesOccupied--
        p1.endMatch(sets.first, sets.second)
        p2.endMatch(sets.second, sets.first)
    }

    /**
     * Добавление нового игрока.
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     * Проверка на корректность актуальна: например, может быть такая ситуация, что новому игроку надо сыграть много матчей, а все
     * остальные уже доиграли все свои матчи.
     */
    // TODO проверка, что игрока с таким именем ещё не существует
    @Throws(IncorrectChangeException::class)
    fun addPlayer(player: MutablePlayerState, check: Boolean) {
        if (check) {
            val newAllPlayers = ArrayList(allPlayers) + player
            if (!createCurrentSimulation(newAllPlayers, tournamentMatchesPerPlayerCnt).isCorrectNow()) {
                throw IncorrectChangeException("Если добавить нового игрока, то турнир не сходится")
            }
        }

        allPlayers += player
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
        if (check) {
            if (player.isPlaysNow() || (player.matchesFinishedCnt > 0)) {
                throw IncorrectChangeException("Невозможно удалить из турнира этого игрока, так как он уже начал играть")
            }
            val newAllPlayers = ArrayList(allPlayers) - player
            if (!createCurrentSimulation(newAllPlayers, tournamentMatchesPerPlayerCnt).isCorrectNow()) {
                throw IncorrectChangeException("Если удалить из турнира этого игрока, то турнир не сходится")
            }
        }

        if (!allPlayers.remove(player)) {
            throw IncorrectChangeException("Игрок, которого мы пытаемся удалить, не найден в списках турнира")
        }
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
        if (check) {
            if (allPlayers.any { it.matchesStartedCnt > newTournamentMatchesPerPlayerCnt }) {
                throw IncorrectChangeException("Невозможно установить такое количество матчей на турнире: кто-то уже сыграл больше матчей")
            }
            val s = createCurrentSimulation(allPlayers, newTournamentMatchesPerPlayerCnt)
            if (!s.isCorrectNow()) {
                throw IncorrectChangeException("Если поменять таким образом количество матчей на турнире, то он перестаёт сходиться")
            }
        }

        tournamentMatchesPerPlayerCnt = newTournamentMatchesPerPlayerCnt
    }

    /**
     * Изменение общего количества столов на турнире в данный момент.
     * Если количество столов уменьшено и получается, что занято больше столов, чем есть в наличии, это считается корректным.
     */
    fun changeOverallTablesCnt(newOverallTablesCnt: Int) {
        tablesCnt = newOverallTablesCnt
    }

    /**
     * Сгенерировать наилучший матч, который можно сыграть в текущих обстоятельствах, и начать его.
     * В случае, если при текущих ожидающих игроках никакой следующий матч не будет корректным, либо нет свободных столов,
     * возвращает `null`.
     * В случае, если турнир уже сейчас некорректный и не может сойтись ни при каких условиях, кидает исключение [IncorrectChangeException].
     * TODO реализовать выдачу исключения, а также проверку свободных столов перенести сюда
     */
    @Throws(IncorrectChangeException::class)
    fun generateAndStartMatch(): Pair<MutablePlayerState, MutablePlayerState>? =
        generateNextMatch()?.also { startMatch(it.first.name to it.second.name) }

    fun hasFreeTables() = tablesOccupied < tablesCnt

    fun generateCurrentRanking() = ranker.generate(allPlayers)

    companion object {

        private fun createCurrentSimulation(allPlayers: List<MutablePlayerState>, tournamentMatchesPerPlayerCnt: Int): Simulation {
            val s = Simulation(allPlayers, tournamentMatchesPerPlayerCnt)
            allPlayers.forEach { p1 ->
                p1.getAllPlayersPlayedOrStarted().forEach { p2 ->
                    s.play(p1 to p2, false)
                }
            }
            return s
        }

        private fun listAllPairs(curEligible: List<MutablePlayerState>) = curEligible
            .flatMapIndexed { i1, player1 ->
                curEligible
                    .filterIndexed { i2, _ -> i2 > i1 }
                    .map { player2 -> player1 to player2 }
            }

    }

}
