package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitContentMessage
import dev.inmo.tgbotapi.types.buttons.KeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.utils.EntitiesBuilderBody
import dev.inmo.tgbotapi.utils.buildEntities
import fastSwiss.api.IncorrectChangeException
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.Ranking
import kotlinx.coroutines.flow.firstOrNull

/**
 * Логика:
 * - вопроса, который задаёт бот;
 * - и обработки ответа пользователя на этот вопрос.
 *
 * [A] — формат ответа пользователя.
 */
class Dialog<A, R : Ranking>(

    /** Текст вопроса, который задаёт бот в ответ на команду пользователя (или в ответ на какое-то предыдущее сообщение пользователя. */
    val botText: String,

    /** Какую клавиатуру выставляем пользователю для ответа. */
    val replyMarkup: KeyboardMarkup? = null,

    /**
     * Как из ответа пользователя получить [A].
     *
     * `null` должен возвращаться в случае, если не удалось получить корректный ответ.
     * В этом случае, [logic] не будет вызвана, а вместо этого отобразится ошибка.
     */
    val answerExtractor: (CommonMessage<MessageContent>) -> A?,

    /**
     * Логика обработки ответа пользователя в случае, если удалось извлечь ответ в корректном формате.
     * Передаётся:
     * - сообщение пользователя, который дал ответ
     * - текущий турнир
     * - непосредственно ответ пользователя, приведённый к типу [A]
     *
     * Возвращается обновлённый турнир.
     */
    val logic: suspend BehaviourContext.(CommonMessage<MessageContent>, MutableTournament<R>, A) -> MutableTournament<R>,

    /** Нужно ли по итогам ответа пользователя боту вывести какое-то своё сообщение? Если нет, то `null`. */
    val finalMessage: ((A) -> EntitiesBuilderBody)?,

    /** Надо ли вслед за успешным выполнением всего вышеперечисленного вывести информацию о турнире? */
    val shouldOutputTournamentInfo: Boolean,

    /**
     * Надо ли попытаться сгенерировать очередные матчи для турнира (в случае, если он уже стартовал),
     * либо констатировать, что турнир завершён.
     */
    val shouldGenerateMatchesIfTournamentStarted: Boolean,
)


suspend fun <A, R : Ranking> BehaviourContext.runDialog(
    userQueryMessage: ContentMessage<MessageContent>,
    t: MutableTournament<R>,
    d: Dialog<A, R>,
): MutableTournament<R> {

    reply(
        to = userQueryMessage,
        text = d.botText,
        replyMarkup = d.replyMarkup,
    )

    val userAnswerMessage = waitContentMessage().firstOrNull()
    val answer = (userAnswerMessage?.let { d.answerExtractor(it) } ?: return t) as A // TODO: вывести ошибку, что некорректный ввод юзера
    try {
        val res = d.logic(this, userAnswerMessage, t, answer)

        d.finalMessage?.let {
            reply(userAnswerMessage, buildEntities("", it(answer)))
        }

        if (d.shouldOutputTournamentInfo) {
            outputTournamentInfoMessage(userAnswerMessage.chat, res)
        }

        if (d.shouldGenerateMatchesIfTournamentStarted) {
            // TODO
        }

        return res
    } catch (e: IncorrectChangeException) {

        reply(userAnswerMessage, buildEntities("") { +"Ошибка: ${e.message}" })
        return t

    }
}

