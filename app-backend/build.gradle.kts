plugins {
    id("jeudutao.java-conventions")
    id("io.spring.dependency-management")
}

tasks.named("jar") {
    enabled = true
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.liquibase:liquibase-core")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("com.h2database:h2")
}
