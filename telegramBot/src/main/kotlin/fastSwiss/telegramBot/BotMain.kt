package fastSwiss.telegramBot

import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import fastSwiss.api.MutableTournament
import fastSwiss.api.tournamentTypes.topological.TopologicalPairSorter
import fastSwiss.api.tournamentTypes.topological.TopologicalRanker
import fastSwiss.api.tournamentTypes.topological.TopologicalRanking
import fastSwiss.telegramBot.Constants.TELEGRAM_BOT_TOKEN
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.concurrent.thread

val RANKER = TopologicalRanker()
val PAIR_SORTER = TopologicalPairSorter()

suspend fun main() {
    val port = System.getenv("http.port").toIntOrNull()
    println("Main started!!11 on port $port")
    val tournaments = mutableMapOf<Long, MutableTournament<TopologicalRanking>>()

    // стартуем сервер, чтобы показывать полную таблицу
    thread {
        println("Thread started!!11")
        embeddedServer(Netty, port!!) {
            println("Server started!!11")
            routing {
                get("/table/{chatId}") {
                    println("Server got!!11")
                    val chatId = call.parameters["chatId"]?.toLongOrNull() ?: run {
                        call.respondText(
                            "Неверный идентификатор чата: '${call.parameters["chatId"]}'. Идентификатор должен быть числовым",
                            ContentType.Text.Html
                        )
                        return@get
                    }

                    tournaments[chatId]?.let {
                        if (!it.isTournamentStarted) {
                            call.respondText("Турнир не запущен", ContentType.Text.Html)
                            return@get
                        }

                        call.respondText(it.generateCurrentRanking().outputRankingAsHtml(), ContentType.Text.Html)
                    } ?: call.respondText("В чате $chatId не было взаимодействия с ботом", ContentType.Text.Html)
                }
            }
        }.start(wait = true)
    }

    // стартуем бота
    telegramBot(TELEGRAM_BOT_TOKEN).buildBehaviourWithLongPolling {
        CommandsEnum.values().forEach { v ->
            onCommand(v.commandName) {
                val chatId = it.chat.id.chatId
                val t = tournaments.getOrDefault(chatId, MutableTournament(RANKER, PAIR_SORTER))
                tournaments[chatId] = runInteraction(it, t, v.interaction)
            }
        }
    }.join()
}
