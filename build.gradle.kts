plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.repsy.io/mvn/uakihir0/public") }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("work.socialhub.kbsky:core:0.0.1-SNAPSHOT")
    implementation("work.socialhub.kbsky:stream:0.0.1-SNAPSHOT")
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.exposed:exposed-core:0.52.0")
    implementation("org.jetbrains.exposed:exposed-crypt:0.52.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.52.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.52.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.52.0")
    implementation("org.jetbrains.exposed:exposed-json:0.52.0")
    implementation("org.jetbrains.exposed:exposed-money:0.52.0")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.52.0")
    implementation("com.h2database:h2:1.3.148")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}