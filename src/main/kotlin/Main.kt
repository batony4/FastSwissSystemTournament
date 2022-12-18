import kotlin.math.abs

// TODO переделать на формат, когда читается входной файл с парами матчей и результатами (если есть).
//  в начале файла задаются параметры:
//  - список игроков,
//  - количество матчей которое должен сыграть каждый игрок,
//  - количество столов
//  после запуска всегда предлагаются новые пары до заполнения всех столов, если это возможно.
//  сами пары надо вручную переносить в этот же файл и потом, после указания результата, перезапускать расчёт
//  сделать простой формат указания результата матча (с любым возможным разделителем, не только двоеточие, а по дефолту — минус),
//  чтобы игроки сами могли легко это заполнять, и объяснить перед турниром, как этим пользоваться

// TODO НОВЫЕ матчи (назначенные только что) в файле помечаются "--->" в начале строки

// TODO файл должен быть полностью на русском (и имена, и конфигурация, и комментарии)

// TODO добавить при запуске вывод текущей таблицы (со всеми очками, сетами, местами, средними очками, средними сетами
//  и матрицей сыгранных игр), а не только предложение новых пар

// TODO отметить, что система очень гибкая (и надо сохранить это в следующих реализациях —
//  при любых изменениях думать о том, сохраняются ли эти свойства):
//  - любому человеку можно зайти в турнир в любой момент. он будет учтён нормально
//  - можно поменять количество доступных столов и это будет учтено моментально
//  - можно поменять количество матчей, которое должен сыграть каждый игрок, и всё будет корректно учтено,
//      если только кто-то не сыграл уже сейчас больше
//  - можно вместо предложенных пар поставить свою собственную по своему желанию и это ок, система сожрёт
//  - можно вносить любые исправления в прошлое (главное не менять имена игроков)
//  - можно указать любые произвольные сыгранные матчи (даже если они не рекомендовались) и это будет учтено нормально
//  вот какой возможности нет — так это исключить игрока из турнира, если он уже сыграл хотя бы один матч.


fun generateNextMatch(allPlayers: List<Player>, tournamentMatchesPerPlayerCnt: Int): Pair<Player, Player>? {
    val sorted = allPlayers.sorted()
    val allEligible = sorted
        .filter { !it.isPlaysNow() }
        .filter { it.matchesPlayed < tournamentMatchesPerPlayerCnt }

    for (maxMatchesPlayed in 0 until tournamentMatchesPerPlayerCnt) {
        val curEligible = allEligible.filter { it.matchesPlayed <= maxMatchesPlayed }

        val bestMatch = curEligible
            .flatMap { player1 ->
                curEligible
                    .filter { it != player1 }
                    .map { player2 -> player1 to player2 }
            }
            .filter { (player1, player2) -> // проверяем, что не играли раньше
                player1.matchResults.none { it.value.otherPlayer == player2.name }
            }
            // TODO когда сравниваю, учитывать не только разницу мест,
            //  но и разницу очков, сетов и Бергера, а то сейчас выбрал пару соседних мест, но с разными очками.
            //  не забыть про возможное деление на ноль — проставлять средние значения, если матчей сыграно не было
            .minByOrNull { (player1, player2) -> abs(sorted.indexOf(player1) - sorted.indexOf(player2)) }

        if (bestMatch != null) {
            return bestMatch
        }
    }

    return null
}

fun main() {

    val players = listOf(
        Player("A"),
        Player("B"),
        Player("C"),
        Player("D"),
        Player("E"),
        Player("F"),
        Player("G"),
        Player("H"),
        Player("I"),
        Player("J"),
        Player("K"),
        Player("L"),
    )

    val match1 = generateAndStartMatch(players)
    val match2 = generateAndStartMatch(players)
    generateAndStartMatch(players)
    generateAndStartMatch(players)
    endMatch(match1!!, 3, 0)
    generateAndStartMatch(players)
    endMatch(match2!!, 3, 1)
    generateAndStartMatch(players)
    generateAndStartMatch(players)
}

private fun generateAndStartMatch(players: List<Player>): Pair<Player, Player>? {
    val match = generateNextMatch(players, 8)
    match?.let { startMatch(it) } ?: println("Не удалось сгенерировать матч")
    return match
}

fun startMatch(match: Pair<Player, Player>) {
    println("Начинаем матч: ${match.first} — ${match.second}")
    match.first.startMatchWith(match.second.name)
    match.second.startMatchWith(match.first.name)
}

fun endMatch(match: Pair<Player, Player>, sets1: Int, sets2: Int) {
    println("Заканчиваем матч: ${match.first} — ${match.second} ($sets1:$sets2)")
    match.first.endMatch(sets1, sets2)
    match.second.endMatch(sets2, sets1)
}
