package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.utils.EntitiesBuilderBody
import dev.inmo.tgbotapi.utils.buildEntities
import fastSwiss.api.IncorrectChangeException
import kotlinx.coroutines.flow.firstOrNull

suspend fun <T> BehaviourContext.processReply(
    answerConverter: (CommonMessage<MessageContent>) -> T?,
    logic: (T) -> Unit,
    formatAnswer: (T) -> EntitiesBuilderBody,
    outputTournamentInfo: Boolean,
) {
    val message = waitContentMessage().firstOrNull()
    val answer = (message?.let { answerConverter(it) } ?: return) as T
    try {
        logic(answer)
        reply(message, buildEntities("", formatAnswer(answer)))
        if (outputTournamentInfo) tournamentInfoMessage()
    } catch (e: IncorrectChangeException) {
        reply(message, buildEntities("") { +"Ошибка: ${e.message}" })
    }
}

// TODO вывод текущей инфы о турнире
//      если он ещё не начат — то настройки и список игроков, а также дока по запуску турнира
//      если он уже начат — то таблица и матчи, которые сейчас играются, а также настройки и дока по продолжению турнира
fun tournamentInfoMessage() {
    TODO("not implemented")
//    "- будет задействовано " + underline("$tablesCnt полей") + ";\n" +
//            "- каждый сыграет по " + formatTournamentSetting("$matchesCnt матчей") + ".\n\n" +
//            "Эти настройки можно поменять в любой момент (как до старта, так и во время турнира), командами /fieldsCount и /matchesCount соответственно.\n" +
//            "Теперь добавляйте игроков на турнир командой /addPlayer.\n" +
//            "Когда все игроки будут добавлены, запустите турнир командой /startTournament."
}
