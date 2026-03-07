plugins {
    id("jeudutao.java-conventions")
    id("org.springframework.boot")
}

val frontendDist by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(project(":app-backend"))
    frontendDist(project(path = ":app-frontend", configuration = "frontendDist"))

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
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
