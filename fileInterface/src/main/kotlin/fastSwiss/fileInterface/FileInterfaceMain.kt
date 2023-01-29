package fastSwiss.fileInterface

import fastSwiss.api.MutablePlayerState
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.topological.TopologicalPairSorter
import fastSwiss.api.tournamentTypes.topological.TopologicalRanker
import java.io.File
import java.io.PrintWriter
import java.util.*

object FileInterfaceMain {

    @JvmStatic
    fun main(args: Array<String>) {
        val inputFile = File("tournament.txt")
        val outputFile = File("tournament_out.txt")

        val pw = PrintWriter(outputFile)
        val t = parse(inputFile, pw)

        while (t.hasFreeTables()) {
            t.generateAndStartMatch()?.also { pw.println("$GO_TO_TABLE_PREFIX${it.first.name} ${it.second.name}") }
                ?: break // не удалось поставить никого за стол — значит прерываем досрочно
        }
        pw.close()

        inputFile.delete()
        outputFile.renameTo(inputFile)

        t.generateCurrentRanking().outputRanking()
    }


    private fun parse(inputFile: File, copyTo: PrintWriter): MutableTournament<*> {
        val t = MutableTournament(RANKER, PAIR_SORTER)
        var handicapToursCnt = 1

        val sc = Scanner(inputFile)
        while (sc.hasNextLine()) {
            val line = sc.nextLine().removePrefix(GO_TO_TABLE_PREFIX)
            copyTo.println(line)

            val lineTrimmed = line.trim()
            if (lineTrimmed.isBlank()) { // пропускаем пустые строки
                continue
            } else if (lineTrimmed.startsWith("#")) { // пропускаем комменты
                continue
            } else if (lineTrimmed.lowercase().startsWith("Стол".lowercase())) { // Столов
                t.changeOverallTablesCnt(lineTrimmed.split(" ").last().toInt())
            } else if (lineTrimmed.lowercase().startsWith("Матч".lowercase())) { // Матчей
                t.changeTournamentMatchesPerPlayerCnt(lineTrimmed.split(" ").last().toInt(), false)
            } else if (lineTrimmed.lowercase().startsWith("Гандикап".lowercase())) { // ГандикапИгр
                handicapToursCnt = lineTrimmed.split(" ").last().toInt()
            } else if (lineTrimmed.lowercase().startsWith("Игрок".lowercase())) { // Игрок
                val tok = lineTrimmed.split(" ")
                val name = tok[1].removePrefix("-")
                val isPaused = tok[1].startsWith("-")
                val handicapWins = tok.getOrNull(2)?.toInt() ?: 0
                val handicapLosses = tok.getOrNull(3)?.toInt() ?: 0
                t.addPlayer(MutablePlayerState(name, isPaused, handicapToursCnt, handicapWins, handicapLosses), false)
            } else if (lineTrimmed.split(" ").first().let { name -> t.findPlayerByName(name) != null }) { // Результат матча
                // если игрок уже есть в списке, то просто добавляем результаты
                parseMatchLine(t, lineTrimmed)
            } else {
                throw IllegalArgumentException("Не могу разобрать строку: '$lineTrimmed'")
            }
        }
        sc.close()

        return t
    }

    private fun parseMatchLine(t: MutableTournament<*>, line: String) {
        val tok = line.split(" ")
        val p = tok[0] to tok[1]

        // начинаем матч
        t.startMatch(p, false)

        when (tok.size) {

            2 -> { // незавершённый матч
                return
            }

            4 -> { // результаты матча
                val sets1 = line.split(" ")[2].toInt()
                val sets2 = line.split(" ")[3].toInt()
                t.endMatch(p, sets1 to sets2, false)
            }

            else -> throw IllegalArgumentException("Неверный формат строки: '$line'")

        }
    }

    private const val GO_TO_TABLE_PREFIX = "К СТОЛУ --> "

    private val RANKER = TopologicalRanker()
    private val PAIR_SORTER = TopologicalPairSorter()
}
