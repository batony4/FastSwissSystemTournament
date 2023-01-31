package fastSwiss.telegramBot

import dev.inmo.tgbotapi.types.message.textsources.bold
import dev.inmo.tgbotapi.types.message.textsources.underline

fun formatTournamentSetting(setting: String) = underline(setting)
fun formatPlayerName(name: String) = bold(name)
