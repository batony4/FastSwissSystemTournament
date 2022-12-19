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

// TODO Объяснить значения всех полей в таблице. Например:
//  - что такое коэффициент Бергера и как происходит сортировка (4 параметра),
//  - что за числа в скобках,
//  - что значит звёздочка в столбце с количеством сыгранных матчей

// TODO Диктофон! Реализовать алгоритм, который будет гарантировать, что всегда найдётся, с кем поиграть

fun generateNextMatch(allPlayers: List<Player>, tournamentMatchesPerPlayerCnt: Int): Pair<Player, Player>? {
    val sorted = allPlayers.sorted().reversed()
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
                player1.matchResults.none { it.value.otherPlayer == player2 }
            }
            // TODO когда сравниваю, учитывать не только разницу мест,
            //  но и разницу очков, сетов и Бергера, а то сейчас выбрал пару соседних мест, но с разными очками.
            //  не забыть про возможное деление на ноль — проставлять средние значения, если матчей сыграно не было
            .sortedBy { (player1, player2) -> abs(sorted.indexOf(player1) - sorted.indexOf(player2)) }
            .firstOrNull { (player1, player2) -> // пробуем симулировать до конца

                val m = Array(allPlayers.size) { BooleanArray(allPlayers.size) }
                val cnt = Array(allPlayers.size) { 0 }

                allPlayers.forEach { p1 ->
                    val i1 = allPlayers.indexOf(p1)
                    p1.matchResults.forEach { (_, result) ->
                        val i2 = allPlayers.indexOf(result.otherPlayer)
                        m[i1][i2] = true
                        cnt[i1]++
                    }

                    p1.activeMatchWith?.let { p2 ->
                        val i2 = allPlayers.indexOf(p2)
                        m[i1][i2] = true
                        cnt[i1]++
                    }
                }

                val player1Index = allPlayers.indexOf(player1)
                val player2Index = allPlayers.indexOf(player2)
                m[player1Index][player2Index] = true
                m[player2Index][player1Index] = true
                cnt[player1Index]++
                cnt[player2Index]++

                val res = try {
                    rec(m, cnt, tournamentMatchesPerPlayerCnt)
                } finally {
                    m[player1Index][player2Index] = false
                    m[player2Index][player1Index] = false
                    cnt[player1Index]--
                    cnt[player2Index]--
                }

                res
            }

        if (bestMatch != null) {
            return bestMatch
        }
    }

    return null
}

/**
 * Пытаемся симулировать, получится ли полностью составить план матчей с учётом этого матча.
 */
fun rec(m: Array<BooleanArray>, cnt: Array<Int>, tournamentMatchesPerPlayerCnt: Int): Boolean {
    var hasGamesToPlay = false
    for (i in m.indices) {
        if (cnt[i] >= tournamentMatchesPerPlayerCnt) continue
        hasGamesToPlay = true

        for (j in i + 1 until m.size) {
            if (cnt[j] >= tournamentMatchesPerPlayerCnt) continue

            if (!m[i][j]) {
                m[i][j] = true
                m[j][i] = true
                cnt[i]++
                cnt[j]++

                val res = rec(m, cnt, tournamentMatchesPerPlayerCnt)

                m[i][j] = false
                m[j][i] = false
                cnt[i]--
                cnt[j]--

                if (res) {
                    return true
                }
            }
        }
    }

    return !hasGamesToPlay
}

fun startMatch(match: Pair<Player, Player>) {
    match.first.startMatchWith(match.second)
    match.second.startMatchWith(match.first)
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

    outputTable(allPlayers)

}

fun outputTable(allPlayers: ArrayList<Player>) {
    val sorted = allPlayers.sorted().reversed()
    val maxNameLength = allPlayers.maxOf { it.name.length } + 2
    print(
        "Место ".padEnd(6)
                + "Игрок".padEnd(maxNameLength)
                + "Игр".padEnd(5)
                + "Побед".padEnd(11)
                + "Сетов".padEnd(13)
                + "Б-Побед".padEnd(11)
                + "Б-Сетов".padEnd(13)
    )
    repeat(sorted.size) { idx -> print(" ${idx + 1}".padEnd(5)) }
    println()

    for ((index, player) in sorted.withIndex()) {
        print(
            ("" + (index + 1) + ". ").padStart(6)
                    + player.name.padEnd(maxNameLength)
                    + (player.matchesPlayed.toString() + if (player.isPlaysNow()) "*" else "").padEnd(5)
                    + (player.score.wins.toString() + " (%.2f)".format(player.score.winsAvg)).padEnd(11)
                    + ("%+d".format(player.score.setsDiff) + " (%+.2f)".format(player.score.setsDiffAvg)).padEnd(13)
                    + (player.bergerScore.wins.toString() + " (%.2f)".format(player.bergerScore.winsAvg)).padEnd(11)
                    + ("%+d".format(player.bergerScore.setsDiff) + " (%+.2f)".format(player.bergerScore.setsDiffAvg)).padEnd(13)
        )

        for ((otherIndex, otherPlayer) in sorted.withIndex()) {
            val match = player.matchResults[otherPlayer.name]
            if (index == otherIndex) {
                print(" X   ")
            } else if (match != null) {
                print("${match.setsMy}:${match.setsOther}".padEnd(5))
            } else {
                print(" •".padEnd(5))
            }
        }
        println()
    }
}
