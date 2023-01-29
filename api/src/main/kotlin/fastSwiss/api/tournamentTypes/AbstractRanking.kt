package fastSwiss.api.tournamentTypes

import fastSwiss.api.MutablePlayerState

abstract class AbstractRanking : Ranking {

    protected fun outputMatrixForPlayer(player: MutablePlayerState, allPlayersSorted: List<MutablePlayerState>) {
        val index = allPlayersSorted.indexOf(player)
        for ((otherIndex, otherPlayer) in allPlayersSorted.withIndex()) {
            val match = player.matchResults[otherPlayer]
            if (index == otherIndex) {
                print(" X   ")
            } else if (match != null) {
                val delimiter = if ((index > otherIndex) && (match.setsMy > match.setsOther)) {
                    "↑"
                } else if ((index < otherIndex) && (match.setsMy < match.setsOther)) {
                    "↓"
                } else {
                    ":"
                }

                print("${match.setsMy}$delimiter${match.setsOther}".padEnd(5))
            } else {
                print(" •".padEnd(5))
            }
        }
    }

}
