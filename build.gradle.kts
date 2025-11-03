plugins {
    `java-library`
    jacoco
    `maven-publish`
    id("org.jreleaser") version "1.17.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.alubenets"
version = "0.0.3"
description = "A simple wrapper around RestClientResponseException that can contain an instance of HttpRequest inside it."

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
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.7")
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
        inceptionYear.set("2025")
    }

    signing {
        active = org.jreleaser.model.Active.ALWAYS
        armored = true
        verify = true
    }

    release {
        github {
            repoOwner.set("snejokeee")
            repoUrl.set("https://github.com/snejokeee/rest-client-call-exception")
            sign = true
            branch = "master"
            overwrite = true
        }
    }

    deploy {
        maven {
            mavenCentral {
                register("sonatype") {
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
}

publishing {
    publications {
        create<MavenPublication>("library") {
            artifactId = rootProject.name
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/snejokeee/rest-client-call-exception")
                inceptionYear.set("2025")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://spdx.org/licenses/MIT.html")
                    }
                }
                developers {
                    developer {
                        id.set("snejokeee")
                        name.set("Aleksey Lubenets")
                        email.set("an.lubenets@gmail.com")
                        url.set("https://alubenets.dev")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/snejokeee/rest-client-call-exception.git")
                    developerConnection.set("scm:git:ssh://github.com/snejokeee/rest-client-call-exception.git")
                    url.set("https://github.com/snejokeee/rest-client-call-exception")
                }
            }
        }
    }
    repositories {
        maven {
            name = "staging"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}