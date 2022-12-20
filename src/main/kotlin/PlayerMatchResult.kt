/**
 * Результат конкретного матча с точки зрения определённого игрока.
 */
data class PlayerMatchResult(
    val otherPlayer: PlayerState,
    val setsMy: Int,
    val setsOther: Int,
) {
    val winsMy = if (setsMy > setsOther) 1 else 0
}
