plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application{
    mainClass ="com.warlabel.MainKt"
}

group = "dattil.io"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.repsy.io/mvn/uakihir0/public") }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("work.socialhub.kbsky:core:0.3.0-SNAPSHOT")
    implementation("work.socialhub.kbsky:stream:0.3.0-SNAPSHOT")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    testImplementation(kotlin("test"))

    implementation("org.jetbrains.exposed:exposed-core:0.52.0")
    implementation("org.jetbrains.exposed:exposed-crypt:0.52.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.52.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.52.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.52.0")
    implementation("org.jetbrains.exposed:exposed-json:0.52.0")
    implementation("org.jetbrains.exposed:exposed-money:0.52.0")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.52.0")
    implementation("com.h2database:h2:2.2.220")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}
