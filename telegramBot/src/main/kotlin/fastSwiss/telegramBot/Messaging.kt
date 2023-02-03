package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.buttons.KeyboardMarkup
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.textsources.bold
import dev.inmo.tgbotapi.types.message.textsources.underline
import dev.inmo.tgbotapi.utils.EntitiesBuilderBody
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.code
import fastSwiss.api.MutableTournament

// форматирование

fun formatTournamentSetting(setting: String) = underline(setting)
fun formatPlayerName(name: String) = bold(name)

// комплексные процедуры управления диалогом

suspend fun <T> BehaviourContext.processDialog(
    message: ContentMessage<MessageContent>,
    botText: String,
    replyMarkup: KeyboardMarkup? = null,
    answerConverter: (CommonMessage<MessageContent>) -> T?,
    logic: (T) -> Unit,
    formatAnswer: (T) -> EntitiesBuilderBody,
    tournamentInfoToOutput: MutableTournament<*>?,
): Pair<CommonMessage<MessageContent>, ContentMessage<TextContent>>? {
    reply(
        to = message,
        text = botText,
        replyMarkup = replyMarkup,
    )

    return processReply(answerConverter, logic, formatAnswer, tournamentInfoToOutput)
}


suspend fun <T> BehaviourContext.processReply(
    answerConverter: (CommonMessage<MessageContent>) -> T?,
    logic: (T) -> Unit,
    formatAnswer: (T) -> EntitiesBuilderBody,
    tournamentInfoToOutput: MutableTournament<*>?,
): Pair<CommonMessage<MessageContent>, ContentMessage<TextContent>>? {
    TODO()
//    val message = waitContentMessage().firstOrNull()
//    val answer = (message?.let { answerConverter(it) } ?: return null) as T
//    return message to try {
//        logic(answer)
//        reply(message, buildEntities("", formatAnswer(answer))).also {
//            tournamentInfoToOutput?.let { tournament -> outputTournamentInfoMessage(it, tournament) }
//        }
//    } catch (e: IncorrectChangeException) {
//        reply(message, buildEntities("") { +"Ошибка: ${e.message}" })
//    }
}


// TODO если турнир уже начат — то таблица и матчи, которые сейчас играются, а также настройки, ссылка на полную таблицу
suspend fun BehaviourContext.outputTournamentInfoMessage(chat: Chat, t: MutableTournament<*>) {
    val players = t.getPlayersImmutable()
    if (t.isTournamentStarted) {
        sendMessage(chat, buildEntities("") {
            +"" + code(t.generateCurrentRanking().outputRanking())
        })
    } else {
        sendMessage(chat, buildEntities("") {
            +"Настройки турнира:\n" +
                    "• будет задействовано " + formatTournamentSetting("${t.tablesCnt} полей") + " (поменять: /${CommandsEnum.FIELDS_COUNT_COMMAND.commandName});\n" +
                    "• каждый сыграет по " + formatTournamentSetting("${t.tournamentMatchesPerPlayerCnt} матчей") + " (поменять: /$MATCHES_COUNT_COMMAND).\n" +
                    "\n" +
                    (if (players.isEmpty())
                        "Участников нет. Добавьте их: /$ADD_PLAYER_COMMAND\n"
                    else
                        "Участники (добавить: /$ADD_PLAYER_COMMAND, удалить: /$REMOVE_PLAYER_COMMAND):\n") +
                    t.getPlayersImmutable().joinToString("") { "• ${if (it.isPaused) "(пауза) " else ""}${it.name}\n" } +
                    "\n" +
                    "Любые настройки можно поменять как до, так и во время турнира.\n" +
                    "Когда всё будет готово, запустите турнир: /$START_TOURNAMENT_COMMAND."
        })
    }
}
