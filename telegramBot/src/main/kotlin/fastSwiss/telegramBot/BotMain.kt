package fastSwiss.telegramBot

import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.topological.TopologicalPairSorter
import fastSwiss.api.tournamentTypes.topological.TopologicalRanker
import fastSwiss.api.tournamentTypes.topological.TopologicalRanking

// TODO всё привести к этому словарю по всему коду. Сам словарь сохранить в отдельном файле.
/*
Словарь:
- турнир (а не соревнование)
- поле (а не стол)
- матч (а не игра)
- участник (а не игрок)
- счёт (а не сеты)
 */

val RANKER = TopologicalRanker()
val PAIR_SORTER = TopologicalPairSorter()


suspend fun main() {

    // TODO убрать из кода, чтобы было только в файле
    val bot = telegramBot("5890510404:AAFiYNfuAco2e4d_6ODH6bvTkXrHnNOn7Tw")

    bot.buildBehaviourWithLongPolling {
        val tournaments = mutableMapOf<Long, MutableTournament<TopologicalRanking>>()
        CommandsEnum.values().forEach { v ->
            onCommand(v.commandName) {
                val chatId = it.chat.id.chatId
                val t = tournaments.getOrDefault(chatId, MutableTournament(RANKER, PAIR_SORTER))
                tournaments[chatId] = runInteraction(it, t, v.interaction)
            }
        }
    }.join()
}
