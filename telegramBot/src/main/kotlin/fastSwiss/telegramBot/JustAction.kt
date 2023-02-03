package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.utils.EntitiesBuilderBody
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.Ranking

/**
 * Просто действие бота, которое он выполняет в ответ на запрос пользователя. Без ведения диалога.
 */
class JustAction<R : Ranking>(

    /**
     * Логика обработки ответа пользователя в случае, если удалось извлечь ответ в корректном формате.
     * Передаётся:
     * - сообщение с исходным запросом пользователя
     * - текущий турнир
     *
     * Возвращается обновлённый турнир.
     */
    override val logic: suspend BehaviourContext.(ContentMessage<MessageContent>, MutableTournament<R>, Unit) -> MutableTournament<R>,

    /** Нужно ли по итогам ответа пользователя боту вывести какое-то своё сообщение? Если нет, то `null`. */
    override val finalBotMessage: ((Unit) -> EntitiesBuilderBody)?,

    /** Надо ли вслед за успешным выполнением всего вышеперечисленного вывести информацию о турнире? */
    override val shouldOutputTournamentInfo: Boolean,

    /**
     * Надо ли попытаться сгенерировать очередные матчи для турнира (в случае, если он уже стартовал),
     * либо констатировать, что турнир завершён.
     */
    override val shouldGenerateMatchesIfTournamentStarted: Boolean,
) : Interaction<Unit, R>
