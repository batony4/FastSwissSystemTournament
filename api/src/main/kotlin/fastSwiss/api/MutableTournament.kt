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

    var isTournamentStarted: Boolean = false
        private set

    var tablesCnt: Int = 1
        private set

    var tournamentMatchesPerPlayerCnt: Int = 1
        private set

    private var allPlayers = ArrayList<MutablePlayerState>()

    // Текущее состояние турнира

    private var tablesOccupied = 0

    // Матчи, которые сейчас играются

    private val activeMatches = ArrayList<Pair<String, String>>()

    // Внутренние методы

    private fun generateNextMatch(): Pair<MutablePlayerState, MutablePlayerState>? {
        val s = Simulation.createCurrentSimulation(allPlayers, tournamentMatchesPerPlayerCnt)
        val r = ranker.generate(allPlayers)

        val allEligible = allPlayers
            .filter { !it.isPlaysNow() }
            .filter { !it.isPaused }
            .filter { it.getMatchesFinishedCnt() < tournamentMatchesPerPlayerCnt }


        val bestMatch = listAllPairs(allEligible)
            .filter { (player1, player2) -> !player1.isFinishedGameWith(player2) } // проверяем, что не играли раньше
            .sortedBy { (player1, player2) -> pairSorter.assessPair(player1, player2, r) } // оцениваем пару и сортируем по оценке
            .firstOrNull { (player1, player2) -> // пробуем симулировать до конца
                s.isCorrectWithMatch(player1 to player2)
            }

        return bestMatch
    }

    // ----- API -----

    /**
     * Старт турнира.
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     */
    @Throws(IncorrectChangeException::class)
    fun startTournament(check: Boolean) {
        if (isTournamentStarted) {
            throw IncorrectChangeException("Турнир уже начат")
        }

        if (check) {
            if (allPlayers.size < 2) {
                throw IncorrectChangeException("Нужно хотя бы 2 участника для проведения турнира, а сейчас их ${allPlayers.size}")
            }
            if (allPlayers.size - 1 < tournamentMatchesPerPlayerCnt) {
                throw IncorrectChangeException("Чтобы каждый участник сыграл $tournamentMatchesPerPlayerCnt матчей, нужно хотя бы ${tournamentMatchesPerPlayerCnt + 1} участников, а сейчас их ${allPlayers.size}")
            }
        }

        isTournamentStarted = true
    }

    fun findPlayerByName(name: String): MutablePlayerState? = allPlayers.firstOrNull { it.name == name }

    fun getPlayersImmutable(): List<ImmutablePlayerState> = allPlayers

    fun getActiveMatches(): List<Pair<String, String>> = activeMatches

    /**
     * Старт матча между соперниками, имена которых перечислены в [names].
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     */
    @Throws(IncorrectChangeException::class)
    fun startMatch(names: Pair<String, String>, check: Boolean) {
        if (!isTournamentStarted) {
            throw IncorrectChangeException("Необходимо запустить турнир")
        }

        val p1 = findPlayerByName(names.first) ?: throw IncorrectChangeException("Участник ${names.first} не найден")
        val p2 = findPlayerByName(names.second) ?: throw IncorrectChangeException("Участник ${names.second} не найден")

        if (check) {
            val s = Simulation.createCurrentSimulation(allPlayers, tournamentMatchesPerPlayerCnt)
            if (!s.isCorrectWithMatch(p1 to p2)) {
                throw IncorrectChangeException(
                    "Участники ${names.first} и ${names.second} не могут сыграть между собой:" +
                            " при условии проведения этого матча, не сходится сетка турнира"
                )
            }
        }

        tablesOccupied++
        p1.startMatchWith(p2)
        p2.startMatchWith(p1)

        activeMatches += names
    }

    /**
     * Завершение матча между соперниками, имена которых перечислены в [names]. Счёт: [sets].
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     */
    fun endMatch(names: Pair<String, String>, sets: Pair<Int, Int>, check: Boolean) {
        val p1 = findPlayerByName(names.first) ?: throw IncorrectChangeException("Участник ${names.first} не найден")
        val p2 = findPlayerByName(names.second) ?: throw IncorrectChangeException("Участник ${names.second} не найден")

        if (check) {
            if (p1.activeMatchWith != p2) throw IncorrectChangeException("Участник $p1 сейчас не играет с участником $p2")
            if (p2.activeMatchWith != p1) throw IncorrectChangeException("Участник $p2 сейчас не играет с участником $p1")
        }

        tablesOccupied--
        p1.endMatch(sets.first, sets.second)
        p2.endMatch(sets.second, sets.first)

        activeMatches -= names
        activeMatches -= names.second to names.first // На всякий случай, вдруг при создании матча игроки задавались в обратном порядке
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
            if (allPlayers.contains(player)) throw IncorrectChangeException("Участник $player уже добавлен ранее")

            // проверяем сходимость только если турнир начался
            if (isTournamentStarted) {
                val newAllPlayers = ArrayList(allPlayers) + player
                if (!Simulation.createCurrentSimulation(newAllPlayers, tournamentMatchesPerPlayerCnt).isCorrectNow()) {
                    throw IncorrectChangeException("Если добавить нового участника, то турнир не сходится")
                }
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
            ?: throw IncorrectChangeException("Участник, которого мы пытаемся удалить, не найден в турнире")

        if (check) {
            if (player.isPlaysNow() || (player.getMatchesFinishedCnt() > 0)) {
                throw IncorrectChangeException("Невозможно удалить из турнира этого участника, так как он уже дебютировал (уже начал играть свои матчи)")
            }

            // проверяем сходимость только если турнир начался
            if (isTournamentStarted) {
                val newAllPlayers = ArrayList(allPlayers) - player
                if (!Simulation.createCurrentSimulation(newAllPlayers, tournamentMatchesPerPlayerCnt).isCorrectNow()) {
                    throw IncorrectChangeException("Если удалить из турнира этого участника, то сетка турнира не сходится")
                }
            }
        }

        if (!allPlayers.remove(player)) {
            throw IncorrectChangeException("Участник, которого мы пытаемся удалить, не найден в списках турнира")
        }
    }

    /**
     * Приостановка игрока. Это значит, что на него не будут назначаться матчи.
     * Если он сейчас играет — не страшно, он доиграет этот матч и на него не будут назначаться следующие матчи до снятия его с паузы.
     */
    fun pausePlayer(name: String) {
        val player = findPlayerByName(name)
            ?: throw IncorrectChangeException("Участник, которого мы пытаемся приостановить, не найден в турнире")

        player.isPaused = true
    }

    /**
     * Снятие игрока с паузы. Это значит, что на него снова могут назначаться матчи.
     */
    fun unpausePlayer(name: String) {
        val player = findPlayerByName(name)
            ?: throw IncorrectChangeException("Участник, которого мы пытаемся снять с паузы, не найден в турнире")

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
            if (allPlayers.any { it.getMatchesStartedCnt() > newTournamentMatchesPerPlayerCnt }) {
                throw IncorrectChangeException("Невозможно установить такое количество матчей на турнире: кто-то уже провёл больше матчей")
            }

            if (newTournamentMatchesPerPlayerCnt < 1) {
                throw IncorrectChangeException("Количество матчей должно быть больше 0")
            }

            // проверяем сходимость только если турнир начался
            if (isTournamentStarted) {
                if (newTournamentMatchesPerPlayerCnt > allPlayers.size - 1) {
                    throw IncorrectChangeException("Столько матчей не получится сыграть, даже если каждый участник сыграет со всеми остальными")
                }

                if (!Simulation.createCurrentSimulation(allPlayers, newTournamentMatchesPerPlayerCnt).isCorrectNow()) {
                    throw IncorrectChangeException("Если поменять таким образом количество матчей на турнире, то сетка турнира перестаёт сходиться")
                }
            }
        }

        tournamentMatchesPerPlayerCnt = newTournamentMatchesPerPlayerCnt
    }

    /**
     * Изменение общего количества столов на турнире в данный момент.
     * Если количество столов уменьшено и получается, что занято больше столов, чем есть в наличии, это считается корректным.
     * Если [check] == `true`, то сначала будет проведена проверка корректности этого действия и, если оно окажется некорректным,
     * то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то никаких проверок производиться не будет и действие метода будет выполнено в любом случае.
     */
    fun changeOverallTablesCnt(newOverallTablesCnt: Int, check: Boolean) {
        if (check) {
            if (newOverallTablesCnt < 0) {
                throw IncorrectChangeException("Количество полей должно быть не меньше 0")
            }
        }
        tablesCnt = newOverallTablesCnt
    }

    /**
     * Сгенерировать максимум наилучших матчей, которые можно сыграть в текущих обстоятельствах (с учётом числа свободных столов,
     * а также, сходимости турнира), и начать их.
     * В случае, если при текущих ожидающих игроках никакой следующий матч не будет корректным, либо нет свободных столов,
     * возвращает пустой список.
     * Если [check] == `true`, то сначала будет проведена проверка, корректен ли в принципе турнир на данный момент и если окажется,
     * что он не может сойтись уже сейчас, то метод не выполнит никаких действий и вернёт исключение [IncorrectChangeException].
     * Если же [check] == `false`, то проверок сходимости производиться не будет, и метод вернёт
     * просто пустой список в случае некорректного турнира.
     */
    @Throws(IncorrectChangeException::class)
    fun generateAndStartMatches(check: Boolean): List<Pair<MutablePlayerState, MutablePlayerState>> {
        if (check) {
            if (!Simulation.createCurrentSimulation(allPlayers, tournamentMatchesPerPlayerCnt).isCorrectNow()) {
                throw IncorrectChangeException("Сетка турнира не сходится")
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

    /**
     * Турнир закончен, когда все матчи турнира начаты, и при этом активных матчей нет (все закончены).
     */
    fun isTournamentFinished() =
        Simulation.createCurrentSimulation(allPlayers, tournamentMatchesPerPlayerCnt).isAllMatchesStartedNow() && activeMatches.isEmpty()

    fun hasFreeTables() = tablesOccupied < tablesCnt

    fun generateCurrentRanking() = ranker.generate(allPlayers)

    companion object {

        private fun listAllPairs(curEligible: List<MutablePlayerState>) = curEligible
            .flatMapIndexed { i1, player1 ->
                curEligible
                    .filterIndexed { i2, _ -> i2 > i1 }
                    .map { player2 -> player1 to player2 }
            }

    }

}
