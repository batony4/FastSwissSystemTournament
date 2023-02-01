package fastSwiss.telegramBot


val CREATE_TOURNAMENT_COMMAND = "create_tournament"
val FIELDS_COUNT_COMMAND = "fields_count"
val MATCHES_COUNT_COMMAND = "matches_count"
val ADD_PLAYER_COMMAND = "add_player"
val REMOVE_PLAYER_COMMAND = "remove_player"
val PAUSE_PLAYER_COMMAND = "pause_player"
val UNPAUSE_PLAYER_COMMAND = "resume_player"
val START_TOURNAMENT_COMMAND =
    "go" // TODO запуск турнира (после этого будут предлагаться новые матчи в ответ на любое изменение в турнире, либо надпись "турнир завершён")
val MATCH_RESULT_COMMAND = "result" // TODO реализовать

/**
 * Выводит список команд для регистрации в BotFather.
 */
fun main() {
    println("$CREATE_TOURNAMENT_COMMAND - Начать настройку нового турнира")
    println("$FIELDS_COUNT_COMMAND - Настроить количество полей")
    println("$MATCHES_COUNT_COMMAND - Настроить количество матчей")
    println("$ADD_PLAYER_COMMAND - Добавить участника")
    println("$REMOVE_PLAYER_COMMAND - Удалить участника")
    println("$PAUSE_PLAYER_COMMAND - Временно не назначать участника на новые матчи")
    println("$UNPAUSE_PLAYER_COMMAND - Снова назначать участника на новые матчи")
    println("$START_TOURNAMENT_COMMAND - Запустить турнир")
    println("$MATCH_RESULT_COMMAND - Указать результат матча")
}
