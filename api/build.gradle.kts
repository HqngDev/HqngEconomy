plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

// Publish a clean, unshaded JAR — this is the API artifact other plugins depend on
tasks.jar {
    archiveBaseName.set("HqngEconomy-API")
    archiveClassifier.set("")
}
