package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.buttons.ReplyForce
import dev.inmo.tgbotapi.utils.row
import fastSwiss.api.ImmutablePlayerState
import kotlin.math.min

/**
 * Разные виды клавиатур, которые подставляются вместе с сообщениями от бота.
 */
object Keyboards {

    /**
     * Просто автоматически подставляет автору сообщения цитирование последнего сообщения от бота.
     * Клавиатура при этом не меняется.
     */
    fun replyForce() = ReplyForce(selective = true)

    /**
     * Обобщённый метод для создания любой клавиатуры с [buttonsPerLine] кнопками в строке.
     */
    fun replyKeyboardOf(buttons: List<String>, buttonsPerLine: Int) =
        replyKeyboard(resizeKeyboard = true, oneTimeKeyboard = true, selective = true) {
            for (i in buttons.indices step buttonsPerLine) {
                row {
                    buttons.subList(i, min(buttons.size, i + buttonsPerLine)).forEach { simpleButton(it) }
                }
            }
        }

    fun replyKeyboardOfPlayers(
        players: List<ImmutablePlayerState>,
        buttonsPerLine: Int = 4,
    ) = replyKeyboardOf(players.map { it.name }, buttonsPerLine)

    fun replyKeyboard1to16() = replyKeyboardOf((1..16).map { it.toString() }, 8)
}
