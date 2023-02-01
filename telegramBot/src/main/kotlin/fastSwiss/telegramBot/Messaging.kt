package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitContentMessage
import dev.inmo.tgbotapi.types.buttons.KeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.EntitiesBuilderBody
import dev.inmo.tgbotapi.utils.buildEntities
import fastSwiss.api.IncorrectChangeException
import fastSwiss.api.MutableTournament
import kotlinx.coroutines.flow.firstOrNull


suspend fun <T> BehaviourContext.processDialog(
    message: ContentMessage<TextContent>,
    botText: String,
    replyMarkup: KeyboardMarkup? = null,
    answerConverter: (CommonMessage<MessageContent>) -> T?,
    logic: (T) -> Unit,
    formatAnswer: (T) -> EntitiesBuilderBody,
    tournamentInfoToOutput: MutableTournament<*>?,
): ContentMessage<TextContent>? {
    reply(
        to = message,
        text = botText,
        replyMarkup = replyMarkup,
    )

    return processReply(answerConverter, logic, formatAnswer, tournamentInfoToOutput)
}


private suspend fun <T> BehaviourContext.processReply(
    answerConverter: (CommonMessage<MessageContent>) -> T?,
    logic: (T) -> Unit,
    formatAnswer: (T) -> EntitiesBuilderBody,
    tournamentInfoToOutput: MutableTournament<*>?,
): ContentMessage<TextContent>? {
    val message = waitContentMessage().firstOrNull()
    val answer = (message?.let { answerConverter(it) } ?: return null) as T
    return try {
        logic(answer)
        reply(message, buildEntities("", formatAnswer(answer))).also {
            tournamentInfoToOutput?.let { tournament -> tournamentInfoMessage(it, tournament) }
        }
    } catch (e: IncorrectChangeException) {
        reply(message, buildEntities("") { +"Ошибка: ${e.message}" })
    }
}


// TODO вывод текущей инфы о турнире
//      если он ещё не начат — то настройки и список игроков, а также дока по запуску турнира
//      если он уже начат — то таблица и матчи, которые сейчас играются, а также настройки и дока по продолжению турнира
private suspend fun BehaviourContext.tournamentInfoMessage(message: ContentMessage<TextContent>, t: MutableTournament<*>) {
    val players = t.getPlayersImmutable()
    reply(message, buildEntities("") {
        +"Настройки турнира:\n" +
                "- будет задействовано " + formatTournamentSetting("${t.tablesCnt} полей") + " (поменять: /$FIELDS_COUNT_COMMAND);\n" +
                "- каждый сыграет по " + formatTournamentSetting("${t.tournamentMatchesPerPlayerCnt} матчей") + " (поменять: /$MATCHES_COUNT_COMMAND).\n" +
                "\n" +
                (if (players.isEmpty())
                    "Участников нет. Добавьте их: /$ADD_PLAYER_COMMAND\n"
                else
                    "Участники (добавить: /$ADD_PLAYER_COMMAND, удалить: /$REMOVE_PLAYER_COMMAND):\n") +
                t.getPlayersImmutable().joinToString { "- ${if (it.isPaused) "(пауза) " else ""}${it.name}\n" } +
                "\n" +
                "Любые настройки можно поменять как до, так и во время турнира.\n" +
                "Когда всё будет настроено, запустите турнир командой /$START_TOURNAMENT_COMMAND."
    })
}
