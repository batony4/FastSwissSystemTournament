package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.buttons.KeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.utils.EntitiesBuilderBody
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.Ranking

/**
 * Логика:
 * - вопроса, который задаёт бот;
 * - и обработки ответа пользователя на этот вопрос.
 *
 * [A] — формат ответа пользователя.
 */
class Dialog<A, R : Ranking>(

    /**
     * Текст вопроса, который задаёт бот в ответ на команду пользователя (или в ответ на какое-то предыдущее сообщение пользователя).
     */
    val botText: String,

    /** Какую клавиатуру выставляем пользователю для ответа. */
    val replyMarkup: ((MutableTournament<R>) -> KeyboardMarkup)? = null,

    /**
     * Как из ответа пользователя получить [A].
     *
     * `null` должен возвращаться в случае, если не удалось получить корректный ответ.
     * В этом случае, [logic] не будет вызвана, а вместо этого отобразится ошибка.
     */
    val answerExtractor: (ContentMessage<MessageContent>) -> A?,

    /**
     * Логика обработки ответа пользователя в случае, если удалось извлечь ответ в корректном формате.
     * Передаётся:
     * - сообщение пользователя, который дал ответ
     * - текущий турнир
     * - непосредственно ответ пользователя, приведённый к типу [A]
     *
     * Возвращается обновлённый турнир.
     */
    override val logic: suspend BehaviourContext.(ContentMessage<MessageContent>, MutableTournament<R>, A) -> MutableTournament<R>,

    /** Нужно ли по итогам ответа пользователя боту вывести какое-то своё сообщение? Если нет, то `null`. */
    override val finalBotMessage: ((A) -> EntitiesBuilderBody)?,

    /** Надо ли вслед за успешным выполнением всего вышеперечисленного вывести информацию о турнире? */
    override val shouldOutputTournamentInfo: Boolean,

    /**
     * Надо ли попытаться сгенерировать очередные матчи для турнира (в случае, если он уже стартовал),
     * либо констатировать, что турнир завершён.
     */
    override val shouldGenerateMatchesIfTournamentStarted: Boolean,
) : Interaction<A, R>
