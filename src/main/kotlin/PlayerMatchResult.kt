data class PlayerMatchResult(
    val otherPlayer: String,
    val setsMy: Int,
    val setsOther: Int,
) {
    val winsMy = if (setsMy > setsOther) 1 else 0
}
