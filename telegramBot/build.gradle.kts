plugins {
    id("fastSwiss.kotlin-application-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-text")
    implementation(project(":api"))
    implementation("dev.inmo:tgbotapi:5.0.1")

    implementation("io.ktor:ktor-server-core:2.2.3")
    implementation("io.ktor:ktor-server-netty:2.2.3")
}

application {
    // Define the main class for the application.
    mainClass.set("fastSwiss.telegramBot.BotMainKt")
}

tasks.jar {
    manifest.attributes["Main-Class"] = "fastSwiss.telegramBot.BotMainKt"
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree) // OR .map { zipTree(it) }
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
