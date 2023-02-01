package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.buttons.ReplyForce
import dev.inmo.tgbotapi.utils.row

fun replyForce() = ReplyForce(selective = true)

fun replyKeyboardOf(buttons: List<String>, buttonsPerLine: Int) =
    replyKeyboard(resizeKeyboard = true, oneTimeKeyboard = true, selective = true) {
        for (i in buttons.indices step buttonsPerLine) {
            row {
                buttons.subList(i, i + buttonsPerLine).forEach { simpleButton(it) }
            }
        }
    }

fun replyKeyboard1to16() = replyKeyboardOf((1..16).map { it.toString() }, 8)
