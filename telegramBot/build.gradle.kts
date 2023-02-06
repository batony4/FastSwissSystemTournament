plugins {
    id("fastSwiss.kotlin-application-conventions")
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10")
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

//tasks.jar {
//    manifest {
//        attributes["Main-Class"] = "fastSwiss.telegramBot.BotMainKt"
//    }
//    // This line of code recursively collects and copies all of a project's files
//    // and adds them to the JAR itself. One can extend this task, to skip certain
//    // files or particular types at will
//    from { configurations.runtimeClasspath.map { it.isDirectory() ? it : zipTree(it) } }
//}

//tasks.jar {
//    manifest.attributes["Main-Class"] = "fastSwiss.telegramBot.BotMainKt"
//    from { configurations.compileClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
//}
