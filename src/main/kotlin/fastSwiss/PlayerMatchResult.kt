package fastSwiss

/**
 * Результат конкретного матча с точки зрения определённого игрока.
 */
data class PlayerMatchResult(
    val otherPlayer: PlayerState,
    val setsMy: Int,
    val setsOther: Int,
) {
    val isWin = setsMy > setsOther
    val winsMy = if (isWin) 1 else 0

    val setsDiff = setsMy - setsOther
}
