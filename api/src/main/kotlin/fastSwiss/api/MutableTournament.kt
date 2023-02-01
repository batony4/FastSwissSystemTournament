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

    var tablesCnt: Int = 1
        private set

    var tournamentMatchesPerPlayerCnt: Int = 1
        private set

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

    fun getPlayersImmutable(): List<ImmutablePlayerState> = allPlayers

    /**
     * Старт матча между соперниками, имена которых перечислены в [names].
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     */
    @Throws(IncorrectChangeException::class)
    fun startMatch(names: Pair<String, String>, check: Boolean) {
        val p1 = findPlayerByName(names.first) ?: throw IncorrectChangeException("Игрок ${names.first} не найден")
        val p2 = findPlayerByName(names.second) ?: throw IncorrectChangeException("Игрок ${names.second} не найден")

        if (check) {
            val s = Simulation(allPlayers, tournamentMatchesPerPlayerCnt)
            if (!s.isCorrectWithMatch(p1 to p2)) {
                throw IncorrectChangeException(
                    "Игроки ${names.first} и ${names.second} не могут сыграть между собой:" +
                            " при условии проведения этого матча, не сходится турнир"
                )
            }
        }

        tablesOccupied++
        p1.startMatchWith(p2)
        p2.startMatchWith(p1)
    }

    /**
     * Завершение матча между соперниками, имена которых перечислены в [names]. Счёт: [sets].
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     */
    fun endMatch(names: Pair<String, String>, sets: Pair<Int, Int>, check: Boolean) {
        val p1 = findPlayerByName(names.first) ?: throw IncorrectChangeException("Игрок ${names.first} не найден")
        val p2 = findPlayerByName(names.second) ?: throw IncorrectChangeException("Игрок ${names.second} не найден")

        if (check) {
            if (p1.activeMatchWith != p2) throw IncorrectChangeException("Игрок $p1 не играет с игроком $p2")
            if (p2.activeMatchWith != p1) throw IncorrectChangeException("Игрок $p2 не играет с игроком $p1")
        }

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
    @Throws(IncorrectChangeException::class)
    fun addPlayer(player: MutablePlayerState, check: Boolean) {
        if (check) {
            if (allPlayers.contains(player)) throw IncorrectChangeException("Игрок $player уже существует")

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
    fun removePlayer(name: String, check: Boolean) {
        val player = findPlayerByName(name)
            ?: throw IncorrectChangeException("Игрок, которого мы пытаемся удалить, не участвует в турнире")

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
     * Приостановка игрока. Это значит, что на него не будут назначаться матчи.
     * Если он сейчас играет — не страшно, он доиграет этот матч и на него не будут назначаться следующие матчи до снятия его с паузы.
     */
    fun pausePlayer(name: String) {
        val player = findPlayerByName(name)
            ?: throw IncorrectChangeException("Игрок, которого мы пытаемся приостановить, не найден в списках турнира")

        player.isPaused = true
    }

    /**
     * Снятие игрока с паузы. Это значит, что на него снова могут назначаться матчи.
     */
    fun unpausePlayer(name: String) {
        val player = findPlayerByName(name)
            ?: throw IncorrectChangeException("Игрок, которого мы пытаемся снять с паузы, не найден в списках турнира")

        player.isPaused = false
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
     * Сгенерировать максимум наилучших матчей, которые можно сыграть в текущих обстоятельствах (с учётом числа свободных столов,
     * а также, сходимости турнира), и начать их.
     * В случае, если при текущих ожидающих игроках никакой следующий матч не будет корректным, либо нет свободных столов,
     * возвращает пусой список.
     * Если [check] == `true`, то сначала будет проведена проверка, корректен ли в принципе турнир на данный момент и если окажется,
     * что он не может сойтись уже сейчас, то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то проверок сходимости производиться не будет, и метод вернёт
     * просто пустой список в случае некорректного турнира.
     */
    @Throws(IncorrectChangeException::class)
    fun generateAndStartMatches(check: Boolean): List<Pair<MutablePlayerState, MutablePlayerState>> {
        if (check) {
            if (!createCurrentSimulation(allPlayers, tournamentMatchesPerPlayerCnt).isCorrectNow()) {
                throw IncorrectChangeException("Турнир не сходится уже сейчас")
            }
        }

        val res = mutableListOf<Pair<MutablePlayerState, MutablePlayerState>>()
        while (hasFreeTables()) {
            val nextMatch = generateNextMatch() ?: break
            startMatch(nextMatch.first.name to nextMatch.second.name, false)
            res.add(nextMatch)
        }
        return res
    }

    fun hasFreeTables() = tablesOccupied < tablesCnt

    fun generateCurrentRanking() = ranker.generate(allPlayers)

    companion object {

        private fun createCurrentSimulation(allPlayers: List<MutablePlayerState>, tournamentMatchesPerPlayerCnt: Int): Simulation {
            val s = Simulation(allPlayers, tournamentMatchesPerPlayerCnt)
            allPlayers.forEachIndexed { idx1, p1 ->
                p1.getAllPlayersPlayedOrStarted()
                    .filter { allPlayers.indexOf(it) > idx1 }
                    .forEach { p2 ->
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
