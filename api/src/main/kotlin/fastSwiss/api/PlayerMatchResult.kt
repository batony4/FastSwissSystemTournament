package fastSwiss.api

/**
 * Результат конкретного матча с точки зрения определённого игрока.
 */
data class PlayerMatchResult(
    val otherPlayer: MutablePlayerState,
    val setsMy: Int,
    val setsOther: Int,
) {
    val isWin = setsMy > setsOther
    val isDraw = setsMy == setsOther

    val winsMy = if (isWin) 1 else 0
    val drawsMy = if (isDraw) 1 else 0
    val pointsMy = winsMy * WIN_POINTS + drawsMy * DRAW_POINTS

    val setsDiff = setsMy - setsOther

    companion object {
        const val WIN_POINTS = 2
        const val DRAW_POINTS = 1
    }
}
