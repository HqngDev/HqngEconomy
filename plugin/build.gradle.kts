plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

kotlin {
    jvmToolchain(21)
}

repositories {
    maven("https://repo.helpch.at/releases")
}

dependencies {
    // Depend on the API module
    implementation(project(":api"))

    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Connection pooling
    implementation("com.zaxxer:HikariCP:7.0.2")

    // MySQL driver
    implementation("com.mysql:mysql-connector-j:9.6.0")

    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.53.2.0")

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.12.2")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("HqngEconomy")
        archiveClassifier.set("")

        // Relocate heavy dependencies to avoid conflicts with other plugins
        relocate("com.zaxxer.hikari", "tech.qhuyy.hqngEconomy.libs.hikari")
        relocate("com.mysql", "tech.qhuyy.hqngEconomy.libs.mysql")
        relocate("org.sqlite", "tech.qhuyy.hqngEconomy.libs.sqlite")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
