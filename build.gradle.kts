plugins {
    kotlin("jvm") version "2.3.21" apply false
    id("com.gradleup.shadow") version "9.4.2" apply false
}

allprojects {
    group = "tech.qhuyy"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
