package fastSwiss.api.tournamentTypes

/**
 * Текущая таблица результатов.
 */
interface Ranking {

    fun outputRanking(): String

    companion object {

        const val HTML_STYLE_MONOSPACE =
            "<style>body { font-family: 'DejaVu Sans Mono', 'Fira Mono', 'Liberation Mono', Monaco, 'Courier New', Courier, monospace }</style>"

    }

}
