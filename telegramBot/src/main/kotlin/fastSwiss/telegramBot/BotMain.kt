@file:Suppress("OPT_IN_USAGE")

package fastSwiss.telegramBot

import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.textsources.underline
import fastSwiss.api.MutablePlayerState
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.topological.TopologicalPairSorter
import fastSwiss.api.tournamentTypes.topological.TopologicalRanker
import fastSwiss.api.tournamentTypes.topological.TopologicalRanking

// TODO всё привести к этому словарю по всему коду. Сам словарь сохранить в отдельном файле.
/*
Словарь:
- турнир (а не соревнование)
- поле (а не стол)
- матч (а не игра)
- участник (а не игрок)
- счёт (а не сеты)
 */

val RANKER = TopologicalRanker()
val PAIR_SORTER = TopologicalPairSorter()


suspend fun main() {

    // TODO убрать из кода, чтобы было только в файле
    val bot = telegramBot("5890510404:AAFiYNfuAco2e4d_6ODH6bvTkXrHnNOn7Tw")

    bot.buildBehaviourWithLongPolling {

        var t = MutableTournament(RANKER, PAIR_SORTER)

        // Начать настройку нового турнира
        onCommand(CREATE_TOURNAMENT_COMMAND) { createTournament(it)?.let { tournament -> t = tournament } }

        onCommand(FIELDS_COUNT_COMMAND) { fieldsCount(it, t) } // Поменять количество полей
        onCommand(MATCHES_COUNT_COMMAND) { matchesCount(it, t) } // Поменять количество матчей
        onCommand(ADD_PLAYER_COMMAND) { addPlayer(it, t) } // Добавить участника
        onCommand(REMOVE_PLAYER_COMMAND) { removePlayer(it, t) } // Удалить участника
        onCommand(PAUSE_PLAYER_COMMAND) { pausePlayer(it, t) } // Временно не назначать участника на новые матчи
        onCommand(UNPAUSE_PLAYER_COMMAND) { unpausePlayer(it, t) } // Снова назначать участника на новые матчи

    }.join()
}


private suspend fun BehaviourContext.createTournament(
    message: CommonMessage<TextContent>,
): MutableTournament<TopologicalRanking>? {
    var tablesCntMutable: Int? = null
    var res: MutableTournament<TopologicalRanking>? = null

    val firstAnswerMessage = processDialog(
        message,
        "Настроим новый турнир. Сколько полей есть в распоряжении?",
        replyKeyboard1to16(),
        { it.text?.toIntOrNull() },
        { tablesCntMutable = it },
        { { +"Отлично, будет задействовано " + underline("$it полей") + "." } },
        null,
    )

    tablesCntMutable?.let { tablesCnt ->
        processDialog(
            firstAnswerMessage!!,
            "А сколько матчей должен сыграть каждый участник за время турнира?",
            replyKeyboard1to16(),
            { it.text?.toIntOrNull() },
            {
                val t = MutableTournament(RANKER, PAIR_SORTER)
                t.changeOverallTablesCnt(tablesCnt)
                t.changeTournamentMatchesPerPlayerCnt(it, true)
                res = t
            },
            { { +"Отлично, новый турнир создан!" } },
            res,
        )
    }

    return res
}


private suspend fun BehaviourContext.fieldsCount(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) =
    processDialog(
        message,
        "Сколько полей есть в распоряжении?",
        replyKeyboard1to16(),
        { it.text?.toIntOrNull() },
        { t.changeOverallTablesCnt(it) },
        { { +"Отлично, теперь задействовано " + underline("$it полей") + "." } },
        t,
    )


private suspend fun BehaviourContext.matchesCount(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) =
    processDialog(
        message,
        "Сколько матчей должен сыграть каждый участник за время турнира?",
        replyKeyboard1to16(),
        { it.text?.toIntOrNull() },
        { t.changeTournamentMatchesPerPlayerCnt(it, true) },
        { { +"Отлично, теперь каждый сыграет по " + formatTournamentSetting("$it матчей") + "." } },
        t,
    )


private suspend fun BehaviourContext.addPlayer(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) =
    processDialog(
        message,
        "Введите имя/название участника (повторяться нельзя):",
        replyForce(),
        { it.text },
        {
            val player = MutablePlayerState(it, false, 0, 0, 0)
            t.addPlayer(player, true)
        },
        { { +"Отлично, участник " + formatPlayerName(it) + " добавлен в турнир" } },
        t,
    )


private suspend fun BehaviourContext.removePlayer(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) =
    processDialog(
        message,
        "Введите имя/название участника, которого надо исключить из турнира:",
        replyKeyboardOfPlayers(t.getPlayersImmutable()),
        { it.text },
        { t.removePlayer(it, true) },
        { { +"Отлично, участник " + formatPlayerName(it) + " исключен из турнира" } },
        t,
    )


private suspend fun BehaviourContext.pausePlayer(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) =
    processDialog(
        message,
        "Введите имя/название участника, который отошёл:",
        replyKeyboardOfPlayers(t.getPlayersImmutable().filter { !it.isPaused }),
        { it.text },
        { t.pausePlayer(it) },
        { { +"Отлично, участнику " + formatPlayerName(it) + " пока не будут назначаться новые матчи" } },
        t,
    )


private suspend fun BehaviourContext.unpausePlayer(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) =
    processDialog(
        message,
        "Введите имя/название участника, который вернулся:",
        replyKeyboardOfPlayers(t.getPlayersImmutable().filter { it.isPaused }),
        { it.text },
        { t.pausePlayer(it) },
        { { +"Отлично, участнику " + formatPlayerName(it) + " снова может быть назначен новый матч" } },
        t,
    )
