plugins {
    id("jeudutao.java-spring-conventions")
}

tasks.named("jar") {
    enabled = true
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-websocket-test")
    implementation("org.springframework.security:spring-security-crypto")
}
