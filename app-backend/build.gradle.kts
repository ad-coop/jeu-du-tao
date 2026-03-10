plugins {
    id("jeudutao.java-spring-conventions")
}

tasks.named("jar") {
    enabled = true
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.security:spring-security-crypto")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
