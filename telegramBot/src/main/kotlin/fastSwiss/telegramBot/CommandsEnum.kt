@file:Suppress("OPT_IN_USAGE")

package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.message.textsources.underline
import fastSwiss.api.tournamentTypes.topological.TopologicalRanking
import fastSwiss.telegramBot.Keyboards.replyForce
import fastSwiss.telegramBot.Keyboards.replyKeyboard1to16
import fastSwiss.telegramBot.Keyboards.replyKeyboardOf
import fastSwiss.telegramBot.Keyboards.replyKeyboardOfPlayers

enum class CommandsEnum(
    val commandName: String,
    val commentRus: String,
    val dialog: Dialog<*, TopologicalRanking>,
) {
    CREATE_TOURNAMENT_COMMAND(
        "create_tournament",
        "Начать настройку нового турнира",
        Dialog(
            "Настроим новый турнир. Сколько полей есть в распоряжении?",
            { replyKeyboard1to16() },
            { it.text?.toIntOrNull() },
            { ansMsg, t, fieldsCnt ->

                runDialog(
                    ansMsg, t, Dialog(
                        "А сколько матчей должен сыграть каждый участник за время турнира?",
                        { replyKeyboard1to16() },
                        { it.text?.toIntOrNull() },
                        { _, _, tablesCnt ->
                            val res = fastSwiss.api.MutableTournament(RANKER, PAIR_SORTER)
                            res.changeTournamentMatchesPerPlayerCnt(fieldsCnt, true)
                            res.changeOverallTablesCnt(tablesCnt)
                            res
                        },
                        { { +"Отлично, новый турнир создан!" } },
                        shouldOutputTournamentInfo = true,
                        shouldGenerateMatchesIfTournamentStarted = false,
                    )
                )

            },
            finalMessage = null,
            shouldOutputTournamentInfo = false,
            shouldGenerateMatchesIfTournamentStarted = false,
        ),
    ),

    FIELDS_COUNT_COMMAND(
        "fields_count",
        "Настроить количество полей",
        Dialog(
            "Сколько полей есть в распоряжении?",
            { replyKeyboard1to16() },
            { it.text?.toIntOrNull() },
            { _, t, fieldsCnt -> t.changeOverallTablesCnt(fieldsCnt); t },
            { { +"Отлично, теперь задействовано " + underline("$it полей") + "." } },
            shouldOutputTournamentInfo = true,
            shouldGenerateMatchesIfTournamentStarted = true,
        ),
    ),

    MATCHES_COUNT_COMMAND(
        "matches_count",
        "Настроить количество матчей",
        Dialog(
            "Сколько матчей должен сыграть каждый участник за время турнира?",
            { replyKeyboard1to16() },
            { it.text?.toIntOrNull() },
            { _, t, matchesCnt -> t.changeTournamentMatchesPerPlayerCnt(matchesCnt, true); t },
            { { +"Отлично, теперь каждый сыграет по " + formatTournamentSetting("$it матчей") + "." } },
            shouldOutputTournamentInfo = true,
            shouldGenerateMatchesIfTournamentStarted = true,
        ),
    ),

    ADD_PLAYER_COMMAND(
        "add_player",
        "Добавить участника",
        Dialog(
            "Введите имя/название участника, повторяться нельзя:",
            { replyForce() },
            { it.text },
            { _, t, name -> t.addPlayer(fastSwiss.api.MutablePlayerState(name, false, 0, 0, 0), true); t },
            { { +"Отлично, участник " + formatPlayerName(it) + " добавлен в турнир" } },
            shouldOutputTournamentInfo = true,
            shouldGenerateMatchesIfTournamentStarted = true,
        ),
    ),

    DELETE_PLAYER_COMMAND(
        "delete_player",
        "Удалить участника",
        Dialog(
            "Выберите участника, которого надо исключить из турнира:",
            { t -> replyKeyboardOfPlayers(t.getPlayersImmutable()) },
            { it.text },
            { _, t, name -> t.removePlayer(name, true); t },
            { { +"Отлично, участник " + formatPlayerName(it) + " исключен из турнира" } },
            shouldOutputTournamentInfo = true,
            shouldGenerateMatchesIfTournamentStarted = true,
        ),
    ),

    START_TOURNAMENT_COMMAND(
        "go",
        "Запустить турнир",
        Dialog(
            null,
            null,
            { null },
            { _, t, _ -> t.startTournament(); t },
            { { +"Отлично, турнир запущен!" } },
            shouldOutputTournamentInfo = false,
            shouldGenerateMatchesIfTournamentStarted = true,
        ),
    ),

    MATCH_RESULT_COMMAND(
        "result",
        "Указать результат матча",
        Dialog(
            "Какой матч завершился?",
            { t -> replyKeyboardOf(t.getActiveMatches().map { it.first + " — " + it.second }, 2) },
            // TODO нет проверки существования игроков и даже количества токенов
            { it.text?.split(" ")?.let { tok -> tok[0] to tok[1] } },
            { ansMsg, t, p ->

                runDialog(
                    ansMsg, t, Dialog(
                        "Напишите через пробел два числа: сколько очков набрал ${p.first} и ${p.second}:",
                        { replyForce() },
                        // TODO нет проверки типов и даже количества токенов
                        { it.text?.split(" ")?.let { tok -> tok[0].toInt() to tok[1].toInt() } },
                        { _, _, score ->
                            t.endMatch(p, score, true)
                            t
                        },
                        { { +"Отлично, результат матча записан!" } },
                        shouldOutputTournamentInfo = true,
                        shouldGenerateMatchesIfTournamentStarted = true,
                    )
                )

            },
            null,
            shouldOutputTournamentInfo = false,
            shouldGenerateMatchesIfTournamentStarted = false,
        ),
    ),

    PAUSE_PLAYER_COMMAND(
        "pause_player",
        "Временно не назначать участника на новые матчи",
        Dialog(
            "Введите имя/название участника, который отошёл:",
            { t -> replyKeyboardOfPlayers(t.getPlayersImmutable().filter { !it.isPaused }) },
            { it.text },
            { _, t, name -> t.pausePlayer(name); t },
            { { +"Отлично, участнику " + formatPlayerName(it) + " пока не будут назначаться новые матчи" } },
            shouldOutputTournamentInfo = false,
            shouldGenerateMatchesIfTournamentStarted = false,
        ),
    ),

    UNPAUSE_PLAYER_COMMAND(
        "unpause_player",
        "Снова назначать участника на новые матчи",
        Dialog(
            "Введите имя/название участника, который вернулся:",
            { t -> replyKeyboardOfPlayers(t.getPlayersImmutable().filter { it.isPaused }) },
            { it.text },
            { _, t, name -> t.unpausePlayer(name); t },
            { { +"Отлично, участнику " + formatPlayerName(it) + " снова может быть назначен новый матч" } },
            shouldOutputTournamentInfo = false,
            shouldGenerateMatchesIfTournamentStarted = true,
        ),
    ),

    ;
}

private fun generateDescriptionsForBotFather() {
    CommandsEnum.values().forEach { println("${it.commandName} - ${it.commentRus}") }
}

/**
 * Выводит список команд для регистрации в BotFather.
 */
fun main() {
    generateDescriptionsForBotFather()
}
