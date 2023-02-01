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
    reply(message, buildEntities("") {
        +"Настройки турнира:\n" +
                "- будет задействовано " + formatTournamentSetting("${t.tablesCnt} полей") + ";\n" +
                "- каждый сыграет по " + formatTournamentSetting("${t.tournamentMatchesPerPlayerCnt} матчей") + ".\n" +
                "\n" +
                "Участники:\n" +
                t.getPlayersImmutable().joinToString("\n") { "- ${it.name}" } + // TODO выводить, кто на паузе, а кто сейчас играет.
                "\n\n" +
                // TODO отсюда поменять
                "Эти настройки можно поменять в любой момент (как до старта, так и во время турнира), командами /fieldsCount и /matchesCount соответственно.\n" +
                "Теперь добавляйте игроков на турнир командой /addPlayer.\n" +
                "Когда все игроки будут добавлены, запустите турнир командой /startTournament."
    })
}
