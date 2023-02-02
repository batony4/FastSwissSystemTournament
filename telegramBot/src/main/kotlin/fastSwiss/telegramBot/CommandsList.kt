package fastSwiss.telegramBot


const val CREATE_TOURNAMENT_COMMAND = "create_tournament"
const val FIELDS_COUNT_COMMAND = "fields_count"
const val MATCHES_COUNT_COMMAND = "matches_count"
const val ADD_PLAYER_COMMAND = "add_player"
const val REMOVE_PLAYER_COMMAND = "delete_player"
const val START_TOURNAMENT_COMMAND =
    "go" // TODO запуск турнира (после этого будут предлагаться новые матчи в ответ на любое изменение в турнире, либо надпись "турнир завершён")
const val MATCH_RESULT_COMMAND = "result"
const val PAUSE_PLAYER_COMMAND = "pause_player"
const val UNPAUSE_PLAYER_COMMAND = "unpause"

/**
 * Выводит список команд для регистрации в BotFather.
 */
fun main() {
    println("$CREATE_TOURNAMENT_COMMAND - Начать настройку нового турнира")
    println("$FIELDS_COUNT_COMMAND - Настроить количество полей")
    println("$MATCHES_COUNT_COMMAND - Настроить количество матчей")
    println("$ADD_PLAYER_COMMAND - Добавить участника")
    println("$REMOVE_PLAYER_COMMAND - Удалить участника")
    println("$START_TOURNAMENT_COMMAND - Запустить турнир")
    println("$MATCH_RESULT_COMMAND - Указать результат матча")
    println("$PAUSE_PLAYER_COMMAND - Временно не назначать участника на новые матчи")
    println("$UNPAUSE_PLAYER_COMMAND - Снова назначать участника на новые матчи")
}
