plugins {
    `java-library`
    jacoco
    `maven-publish`
    id("org.jreleaser") version "1.17.0"
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.alubenets"
version = "0.0.2"
description =
    "A simple wrapper around RestClientResponseException that can contain an instance of HttpRequest inside it."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    api("org.springframework:spring-web")
    api("jakarta.servlet:jakarta.servlet-api:6.0.0")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("org.slf4j:slf4j-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework:spring-webmvc")
    testImplementation("org.xmlunit:xmlunit-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
        csv.required = true
    }
    dependsOn(tasks.test)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jreleaser {
    project {
        author("Aleksey Lubenets")
        inceptionYear = "2025"
    }
    signing {
        active = org.jreleaser.model.Active.ALWAYS
        armored = true
        verify = true
    }
    release {
        github {
            repoOwner = "snejokeee"
            sign = true
            branch = "master"
            overwrite = true
        }
    }
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name = project.name
                    description = project.description
                    url = "https://github.com/snejokeee/rest-client-call-exception"
                    inceptionYear = "2025"
                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://spdx.org/licenses/MIT.html"
                        }
                    }
                    developers {
                        developer {
                            id = "snejokeee"
                            name = "Aleksey Lubenets"
                            email = "an.lubenets@gmail.com"
                            url = "https://alubenets.dev"
                        }
                    }
                    scm {
                        connection = "scm:git:https://github.com/snejokeee/rest-client-call-exception.git"
                        developerConnection = "scm:git:ssh://github.com/snejokeee/rest-client-call-exception.git"
                        url = "https://github.com/snejokeee/rest-client-call-exception"
                    }
                }
            }

        }
    }
    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = org.jreleaser.model.Active.ALWAYS
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                snapshotSupported = false
                setAuthorization("Basic")
                sign = true
                checksums = true
                sourceJar = true
                javadocJar = true
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            pom {
                name = project.name
                description = project.description
                url = "https://github.com/snejokeee/rest-client-call-exception"
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://spdx.org/licenses/MIT.html"
                    }
                }
                developers {
                    developer {
                        id = "snejokeee"
                        name = "Aleksey Lubenets"
                        email = "an.lubenets@gmail.com"
                        url = "https://alubenets.dev"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/snejokeee/rest-client-call-exception.git"
                    developerConnection = "scm:git:ssh://github.com/snejokeee/rest-client-call-exception.git"
                    url = "https://github.com/snejokeee/rest-client-call-exception"
                }
            }
        }
    }
    repositories {
        maven {
            name = "staging"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
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