package fastSwiss.api

/**
 * Основные сведения об игроке. Объект не позволяет их менять.
 */
interface ImmutablePlayerState {
    val name: String
    val isPaused: Boolean
    fun isPlaysNow(): Boolean
    val handicapTours: Int
    val handicapWins: Int
    val handicapLosses: Int
}
