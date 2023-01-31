package fastSwiss.telegramBot

import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.textsources.underline
import dev.inmo.tgbotapi.utils.buildEntities
import fastSwiss.api.MutablePlayerState
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.topological.TopologicalPairSorter
import fastSwiss.api.tournamentTypes.topological.TopologicalRanker
import fastSwiss.api.tournamentTypes.topological.TopologicalRanking

val RANKER = TopologicalRanker()
val PAIR_SORTER = TopologicalPairSorter()

suspend fun main() {

    // TODO убрать из кода, чтобы было только в файле
    val bot = telegramBot("5890510404:AAFiYNfuAco2e4d_6ODH6bvTkXrHnNOn7Tw")

    bot.buildBehaviourWithLongPolling {

        var t = MutableTournament(RANKER, PAIR_SORTER)

        onCommand("createTournament") { // Начать настройку нового турнира
            createTournament(it)?.let { tournament -> t = tournament }
        }

        onCommand("fieldsCount") { fieldsCount(it, t) } // Поменять количество полей

        onCommand("matchesCount") { matchesCount(it, t) } // Поменять количество матчей

        onCommand("addPlayer") { addPlayer(it, t) } // Добавить игрока

        // TODO поддержать:
        //  - удаление игрока
        //  - поставить на паузу
        //  - снять с паузы игрока
        //  - запуск турнира (после этого будут предлагаться новые матчи в ответ на любое изменение в турнире, либо надпись "турнир завершён")
        //  - указать результат матча

    }.join()
}

private suspend fun BehaviourContext.createTournament(
    message: CommonMessage<TextContent>,
): MutableTournament<TopologicalRanking>? {
    var tablesCnt: Int? = null
    var res: MutableTournament<TopologicalRanking>? = null

    reply(
        to = message,
        text = "Настроим новый турнир. Сколько полей есть в распоряжении?",
        replyMarkup = keyboard1to16(),
    )

    processReply(
        { it.text?.toIntOrNull() },
        { tablesCnt = it },
        {
            buildEntities("") {
                +"Отлично, будет задействовано " + underline("$it полей") + "."
            }
        },
        false,
    )

    tablesCnt?.let { tablesCnt ->
        reply(
            to = message,
            text = "А сколько матчей должен сыграть каждый участник за время турнира?",
            replyMarkup = keyboard1to16(),
        )

        processReply(
            { it.text?.toIntOrNull() },
            {
                val t = MutableTournament(RANKER, PAIR_SORTER)
                t.changeOverallTablesCnt(tablesCnt)
                t.changeTournamentMatchesPerPlayerCnt(it, true)
                res = t
            },
            {
                buildEntities("") {
                    +"Отлично, новый турнир создан!"
                }
            },
            false,
        )
    }

    return res
}

private suspend fun BehaviourContext.fieldsCount(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) {
    reply(
        to = message,
        text = "Сколько полей есть в распоряжении?",
        replyMarkup = keyboard1to16(),
    )

    processReply(
        { it.text?.toIntOrNull() },
        { t.changeOverallTablesCnt(it) },
        {
            buildEntities("") {
                +"Отлично, теперь задействовано " + underline("$it полей") + "."
            }
        },
        true,
    )
}

private suspend fun BehaviourContext.matchesCount(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) {
    reply(
        to = message,
        text = "Сколько матчей должен сыграть каждый участник за время турнира?",
        replyMarkup = keyboard1to16(),
    )

    processReply(
        { it.text?.toIntOrNull() },
        { t.changeTournamentMatchesPerPlayerCnt(it, true) },
        {
            buildEntities("") {
                +"Отлично, теперь каждый сыграет по " + formatTournamentSetting("$it матчей") + "."
            }
        },
        true,
    )
}

private suspend fun BehaviourContext.addPlayer(
    message: CommonMessage<TextContent>,
    t: MutableTournament<TopologicalRanking>,
) {
    reply(
        to = message,
        text = "Как зовут игрока? Повторяться нельзя",
        replyMarkup = replyForce(),
    )

    processReply(
        { it.text },
        {
            val player = MutablePlayerState(it, false, 0, 0, 0)
            t.addPlayer(player, true)
        },
        {
            buildEntities("") {
                +"Отлично, игрок " + formatPlayerName(it) + " добавлен в турнир"
            }
        },
        true,
    )
}
