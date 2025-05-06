
plugins {
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    kotlin("jvm") version "1.9.0" apply false
    kotlin("plugin.spring") version "1.9.0" apply false
}

allprojects {
    group = "com.bank"
    version = "0.0.1-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}
