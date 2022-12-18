import java.io.File
import java.io.PrintWriter
import java.util.*
import kotlin.math.abs

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

fun startMatch(match: Pair<Player, Player>) {
    match.first.startMatchWith(match.second.name)
    match.second.startMatchWith(match.first.name)
}

fun endMatch(match: Pair<Player, Player>, sets: Pair<Int, Int>) {
    match.first.endMatch(sets.first, sets.second)
    match.second.endMatch(sets.second, sets.first)
}

private fun generateAndStartMatch(players: List<Player>, tournamentMatchesPerPlayerCnt: Int): Pair<Player, Player>? {
    return generateNextMatch(players, tournamentMatchesPerPlayerCnt)?.also { startMatch(it) }
}

/**
 * Возвращает, был ли начат новый незавершённый матч.
 */
private fun parseMatchLine(allPlayers: ArrayList<Player>, line: String): Boolean {
    val tok = line.split(" ")

    val player1 = allPlayers.first { it.name == tok[0] }
    val player2 = allPlayers.first { it.name == tok[1] }

    // начинаем матч
    startMatch(player1 to player2)

    if (tok.size == 2) { // незавершённый матч
        return true
    } else if (tok.size == 4) { // результаты матча
        val sets1 = line.split(" ")[2].toInt()
        val sets2 = line.split(" ")[3].toInt()
        endMatch(player1 to player2, sets1 to sets2)
        return false
    } else {
        throw Exception("Неверный формат строки: '$line'")
    }
}

private const val GO_TO_TABLE_PREFIX = "К СТОЛУ --> "

fun main() {
    var tablesCnt = 1
    var tablesOccupied = 0
    var tournamentMatchesPerPlayerCnt = 1
    val allPlayers = ArrayList<Player>()

    val inputFile = File("tournament.txt")
    val sc = Scanner(inputFile)

    val outputFile = File("tournament_out.txt")
    val pw = PrintWriter(outputFile)

    while (sc.hasNextLine()) {
        val lineRaw = sc.nextLine().removePrefix(GO_TO_TABLE_PREFIX)
        pw.println(lineRaw)

        val line = lineRaw.trim()
        if (line.isBlank()) { // пропускаем пустые строки
            continue
        } else if (line.startsWith("#")) { // пропускаем комменты
            continue
        } else if (line.split(" ").first().let { name -> allPlayers.count { it.name == name } > 0 }) {
            // если игрок уже есть в списке, то просто добавляем результаты
            if (parseMatchLine(allPlayers, line)) tablesOccupied++
        } else if (line.lowercase().startsWith("Стол".lowercase())) {
            tablesCnt = line.split(" ").last().toInt()
        } else if (line.lowercase().startsWith("Матч".lowercase())) {
            tournamentMatchesPerPlayerCnt = line.split(" ").last().toInt()
        } else if (line.lowercase().startsWith("Игрок".lowercase())) {
            allPlayers += Player(line.split(" ").last())
        } else {
            throw IllegalArgumentException("Не могу разобрать строку: '$line'")
        }
    }
    sc.close()

    for (i in tablesOccupied until tablesCnt) {
        generateAndStartMatch(allPlayers, tournamentMatchesPerPlayerCnt)?.let { match ->
            pw.println("$GO_TO_TABLE_PREFIX${match.first.name} ${match.second.name}")
        } ?: break
    }
    pw.close()

    inputFile.delete()
    outputFile.renameTo(inputFile)

}
