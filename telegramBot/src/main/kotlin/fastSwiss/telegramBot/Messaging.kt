package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.chat.Chat
import dev.inmo.tgbotapi.types.message.textsources.bold
import dev.inmo.tgbotapi.types.message.textsources.link
import dev.inmo.tgbotapi.types.message.textsources.underline
import dev.inmo.tgbotapi.utils.buildEntities
import fastSwiss.api.MutableTournament
import fastSwiss.telegramBot.Constants.TABLE_URL_PREFIX

// форматирование

fun formatTournamentSetting(setting: String) = underline(setting)
fun formatPlayerName(name: String) = bold(name)

// комплексные процедуры управления диалогом

suspend fun BehaviourContext.outputTournamentInfoMessage(chat: Chat, t: MutableTournament<*>) {
    val players = t.getPlayersImmutable()
    if (t.isTournamentStarted) {
        sendMessage(chat, buildEntities("") {
            +"" + t.generateCurrentRanking().outputRanking(true) + "\n" +
                    "• минус перед именем — участник на паузе.\n" +
                    "• звёздочка после количества матчей — играет сейчас.\n" +
                    "\n" +
                    "Таблица подробнее: " + link("$TABLE_URL_PREFIX${chat.id.chatId}")
        })
    } else {
        sendMessage(chat, buildEntities("") {
            +"Настройки турнира:\n" +
                    "• будет задействовано " + formatTournamentSetting("${t.tablesCnt} полей") + " (поменять: /${CommandsEnum.FIELDS_COUNT_COMMAND.commandName});\n" +
                    "• каждый сыграет по " + formatTournamentSetting("${t.tournamentMatchesPerPlayerCnt} матчей") + " (поменять: /${CommandsEnum.MATCHES_COUNT_COMMAND.commandName}).\n" +
                    "\n" +
                    (if (players.isEmpty())
                        "Участников нет. Добавьте их: /${CommandsEnum.ADD_PLAYER_COMMAND.commandName}\n"
                    else
                        "Участники (добавить: /${CommandsEnum.ADD_PLAYER_COMMAND.commandName}, удалить: /${CommandsEnum.DELETE_PLAYER_COMMAND.commandName}):\n") +
                    t.getPlayersImmutable()
                        .mapIndexed { i, p -> "${i + 1}. ${if (p.isPaused) "-" else ""}${p.name}" }
                        .joinToString("\n") +
                    "\n" +
                    "Любые настройки можно поменять как до, так и во время турнира.\n" +
                    "Когда всё будет готово, запустите турнир: /${CommandsEnum.START_TOURNAMENT_COMMAND.commandName}."
        })
    }
}
