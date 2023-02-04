package fastSwiss.api.tournamentTypes

import fastSwiss.api.MutablePlayerState

/**
 * Текущая таблица результатов.
 */
interface Ranking {

    fun outputRanking(shortNotFull: Boolean): String

    fun outputRankingAsHtml() =
        HTML_STYLE_MONOSPACE +
                outputRanking(false)
                    .replace("\n", "<br>")
                    .replace(" ", "&nbsp;")

    companion object {

        const val HTML_STYLE_MONOSPACE =
            "<style>body { font-family: 'DejaVu Sans Mono', 'Fira Mono', 'Liberation Mono', Monaco, 'Courier New', Courier, monospace }</style>"

        fun outputMatrixForPlayer(res: StringBuilder, player: MutablePlayerState, allPlayersSorted: List<MutablePlayerState>) {
            val index = allPlayersSorted.indexOf(player)
            for ((otherIndex, otherPlayer) in allPlayersSorted.withIndex()) {
                val match = player.matchResults[otherPlayer]
                if (index == otherIndex) {
                    res.append(" X   ")
                } else if (match != null) {
                    val delimiter = if ((index > otherIndex) && (match.setsMy > match.setsOther)) {
                        "↑"
                    } else if ((index < otherIndex) && (match.setsMy < match.setsOther)) {
                        "↓"
                    } else {
                        ":"
                    }

                    res.append("${match.setsMy}$delimiter${match.setsOther}".padEnd(5))
                } else {
                    res.append(" •".padEnd(5))
                }
            }
        }

    }

}
