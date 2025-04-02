plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "co.edu.uniquindio"
version = "0.0.1-SNAPSHOT"
description = "Breve descripción de la aplicación"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb") // mongoDB
    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0") // swagger
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    implementation("io.jsonwebtoken:jjwt:0.11.5")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5") // Para compatibilidad con JSON
    implementation ("io.jsonwebtoken:jjwt:0.10.7")
    implementation("com.google.firebase:firebase-admin:9.2.0")


}

tasks.withType<Test> {
    useJUnitPlatform()
}