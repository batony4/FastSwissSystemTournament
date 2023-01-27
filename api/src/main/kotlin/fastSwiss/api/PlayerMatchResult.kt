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
    val isLoss = setsMy < setsOther

    val winsMy = if (isWin) 1 else 0
    val drawsMy = if (isDraw) 1 else 0
    val lossesMy = if (isLoss) 1 else 0

    val setsDiff = setsMy - setsOther
}
