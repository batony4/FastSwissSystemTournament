package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.utils.EntitiesBuilderBody
import dev.inmo.tgbotapi.utils.buildEntities
import fastSwiss.api.IncorrectChangeException
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.Ranking
import kotlinx.coroutines.flow.firstOrNull

/**
 * Взаимодействие пользователя с ботом в ответ на какой-то запрос пользователя.
 * Обобщённый интерфейс.
 */
interface Interaction<A, R : Ranking> {

    /**
     * Логика обработки ответа пользователя в случае, если удалось извлечь ответ в корректном формате.
     * Передаётся:
     * - сообщение пользователя, который дал ответ (если диалога не было, то сообщение пользователя, которое вызвало действие)
     * - текущий турнир
     * - непосредственно ответ пользователя, приведённый к типу [A] (Unit в случае отсутствия диалога)
     *
     * Возвращается обновлённый турнир.
     */
    val logic: suspend BehaviourContext.(ContentMessage<MessageContent>, MutableTournament<R>, A) -> MutableTournament<R>

    /** Нужно ли по итогам ответа пользователя боту вывести какое-то своё сообщение? Если нет, то `null`. */
    val finalBotMessage: ((A) -> EntitiesBuilderBody)?

    /** Надо ли вслед за успешным выполнением всего вышеперечисленного вывести информацию о турнире? */
    val shouldOutputTournamentInfo: Boolean

    /**
     * Надо ли попытаться сгенерировать очередные матчи для турнира (в случае, если он уже стартовал),
     * либо констатировать, что турнир завершён.
     */
    val shouldGenerateMatchesIfTournamentStarted: Boolean
}


suspend fun <A, R : Ranking> BehaviourContext.runInteraction(
    userQueryMessage: ContentMessage<MessageContent>,
    t: MutableTournament<R>,
    interaction: Interaction<A, R>,
): MutableTournament<R> {

    val (lastUserMessage, answer) = when (interaction) {
        is Dialog<A, R> -> {
            reply(
                to = userQueryMessage,
                text = interaction.botText,
                replyMarkup = interaction.replyMarkup?.invoke(t),
            )

            val userAnswerMessage = waitContentMessage().firstOrNull()
            if (userAnswerMessage == null) {
                reply(userQueryMessage, buildEntities("") { +"Ошибка: Ответ не получен." })
                return t
            }

            val answer = interaction.answerExtractor(userAnswerMessage)
            if (answer == null) {
                reply(userQueryMessage, buildEntities("") { +"Ошибка: Некорректный ответ." })
                return t
            }

            userAnswerMessage to answer
        }

        is JustAction<R> -> {
            @Suppress("UNCHECKED_CAST")
            userQueryMessage to (Unit as A)
        }

        else -> {
            throw IllegalArgumentException("Unknown interaction type: ${interaction::class}")
        }
    }

    try {

        val res = interaction.logic(this, lastUserMessage, t, answer)

        interaction.finalBotMessage?.let {
            reply(lastUserMessage, buildEntities("", it(answer)))
        }

        if (interaction.shouldOutputTournamentInfo) {
            outputTournamentInfoMessage(userQueryMessage.chat, res)
        }

        if (t.isTournamentStarted && interaction.shouldGenerateMatchesIfTournamentStarted) {
            val matches = t.generateAndStartMatches(true)
            if (matches.isNotEmpty()) {
                sendMessage(userQueryMessage.chat, buildEntities("") {
                    +"На поля приглашаются:\n" +
                            matches.joinToString("") { "• ${it.first.name} — ${it.second.name}\n" }
                })
            } else {
                if (t.isTournamentFinished()) {
                    sendMessage(userQueryMessage.chat, buildEntities("") { +"Турнир завершён!" })
                } else {
                    if (t.hasFreeTables()) {
                        sendMessage(
                            userQueryMessage.chat,
                            buildEntities("") {
                                +"В данный момент невозможно запустить матч между кем-то из ожидающих участников." +
                                        "Необходимо дождаться, когда доиграет кто-то ещё"
                            })
                    } else {
                        sendMessage(userQueryMessage.chat, buildEntities("") { +"Все поля заняты" })
                    }
                }
            }
        }

        return res
    } catch (e: IncorrectChangeException) {

        reply(lastUserMessage, buildEntities("") { +"Ошибка: ${e.message}" })
        return t

    }
}
