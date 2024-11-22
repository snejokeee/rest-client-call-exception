plugins {
    `java-library`
    `maven-publish`
}

group = "dev.alubenets"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api ("org.springframework:spring-web:6.1.14")
    api ("org.springframework.boot:spring-boot-autoconfigure:3.2.11")
    api ("com.google.code.findbugs:jsr305:3.0.2")
    testImplementation ("org.springframework.boot:spring-boot-starter-test:3.2.11")
    testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "ALubenetsRepository"
            val releasesRepoUrl = uri("https://repo.alubenets.dev/releases/")
            val snapshotsRepoUrl = uri("https://repo.alubenets.dev/snapshots/")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = project.properties["repo.alubenets.dev.username"] as String?
                password = project.properties["repo.alubenets.dev.token"] as String?
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}


