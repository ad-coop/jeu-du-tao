plugins {
    id("jeudutao.java-spring-conventions")
    id("org.springframework.boot")
}

val frontendDist by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")

    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":infra-persistence"))
    implementation(project(":infra-web-backend"))

    implementation("org.springframework.security:spring-security-crypto")

    frontendDist(project(path = ":app-frontend", configuration = "frontendDist"))

    implementation("com.h2database:h2")

}

val copyFrontend by tasks.registering(Sync::class) {
    from(frontendDist)
    into(layout.buildDirectory.dir("generated-resources/frontend/static"))
}

sourceSets {
    getByName("main") {
        resources {
            srcDir(layout.buildDirectory.dir("generated-resources/frontend"))
        }
    }
}

tasks.named("processResources") {
    dependsOn(copyFrontend)
}
