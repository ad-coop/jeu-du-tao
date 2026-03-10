plugins {
    id("jeudutao.java-spring-conventions")
}

tasks.named("jar") {
    enabled = true
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
    testRuntimeOnly("com.h2database:h2")
}
