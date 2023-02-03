@file:Suppress("OPT_IN_USAGE")

package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.message.textsources.underline
import fastSwiss.api.tournamentTypes.topological.TopologicalRanking
import fastSwiss.telegramBot.Keyboards.replyKeyboard1to16

enum class CommandsEnum(
    val commandName: String,
    val commentRus: String,
    val dialog: Dialog<*, TopologicalRanking>,
) {
    CREATE_TOURNAMENT_COMMAND(
        "create_tournament",
        "Начать настройку нового турнира",
        Dialog(
            "Настроим новый турнир. Сколько полей есть в распоряжении?",
            replyKeyboard1to16(),
            { it.text?.toIntOrNull() },
            { ansMsg, t, fieldsCnt ->

                runDialog(
                    ansMsg, t, Dialog(
                        "А сколько матчей должен сыграть каждый участник за время турнира?",
                        replyKeyboard1to16(),
                        { it.text?.toIntOrNull() },
                        { _, _, tablesCnt ->
                            val res = fastSwiss.api.MutableTournament(RANKER, PAIR_SORTER)
                            res.changeTournamentMatchesPerPlayerCnt(fieldsCnt, true)
                            res.changeOverallTablesCnt(tablesCnt)
                            res
                        },
                        { { +"Отлично, новый турнир создан!" } },
                        shouldOutputTournamentInfo = true,
                        shouldGenerateMatchesIfTournamentStarted = false,
                    )
                )

            },
            finalMessage = null,
            shouldOutputTournamentInfo = false,
            shouldGenerateMatchesIfTournamentStarted = false,
        ),
    ),

    FIELDS_COUNT_COMMAND(
        "fields_count",
        "Настроить количество полей",
        Dialog(
            "Сколько полей есть в распоряжении?",
            replyKeyboard1to16(),
            { it.text?.toIntOrNull() },
            { _, t, fieldsCnt -> t.changeOverallTablesCnt(fieldsCnt); t },
            { { +"Отлично, теперь задействовано " + underline("$it полей") + "." } },
            shouldOutputTournamentInfo = true,
            shouldGenerateMatchesIfTournamentStarted = true,
        ),
    ),

    START_TOURNAMENT_COMMAND(
        "start_tournament",
        "Запустить турнир",
        Dialog(
            null,
            null,
            { null },
            { _, t, _ -> t.startTournament(); t },
            { { +"Отлично, турнир запущен!" } },
            shouldOutputTournamentInfo = false,
            shouldGenerateMatchesIfTournamentStarted = true,
        ),
    ),

    ;
}


const val MATCHES_COUNT_COMMAND = "matches_count"
const val ADD_PLAYER_COMMAND = "add_player"
const val REMOVE_PLAYER_COMMAND = "delete_player"
const val TOURNAMENT_INFO_COMMAND = "info" // TODO реализовать. просто выводит инфу о турнире
const val START_TOURNAMENT_COMMAND = "go"
const val MATCH_RESULT_COMMAND = "result"
const val PAUSE_PLAYER_COMMAND = "pause_player"
const val UNPAUSE_PLAYER_COMMAND = "unpause"


/**
 * Выводит список команд для регистрации в BotFather.
 */
fun main() {
    generateDescriptionsForBotFather()
//    println("$MATCHES_COUNT_COMMAND - Настроить количество матчей")
//    println("$ADD_PLAYER_COMMAND - Добавить участника")
//    println("$REMOVE_PLAYER_COMMAND - Удалить участника")
//    println("$START_TOURNAMENT_COMMAND - Запустить турнир")
//    println("$MATCH_RESULT_COMMAND - Указать результат матча")
//    println("$PAUSE_PLAYER_COMMAND - Временно не назначать участника на новые матчи")
//    println("$UNPAUSE_PLAYER_COMMAND - Снова назначать участника на новые матчи")
}

private fun generateDescriptionsForBotFather() {
    CommandsEnum.values().forEach { println("${it.commandName} - ${it.commentRus}") }
}
