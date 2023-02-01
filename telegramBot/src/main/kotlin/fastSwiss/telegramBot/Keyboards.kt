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

fun replyKeyboard1to16() = replyKeyboard(resizeKeyboard = true, oneTimeKeyboard = true, selective = true) {
    row {
        simpleButton("1")
        simpleButton("2")
        simpleButton("3")
        simpleButton("4")
        simpleButton("5")
        simpleButton("6")
        simpleButton("7")
        simpleButton("8")
    }
    row {
        simpleButton("9")
        simpleButton("10")
        simpleButton("11")
        simpleButton("12")
        simpleButton("13")
        simpleButton("14")
        simpleButton("15")
        simpleButton("16")
    }
}
