plugins {
    base
}

val pnpmInstall by tasks.registering(Exec::class) {
    inputs.files("package.json", "pnpm-lock.yaml")
    outputs.file("node_modules/.modules.yaml")
    commandLine("pnpm", "install", "--frozen-lockfile")
}

val pnpmBuild by tasks.registering(Exec::class) {
    dependsOn(pnpmInstall)
    inputs.dir("src")
    inputs.files(
        "vite.config.ts",
        "tsconfig.json",
        "tsconfig.app.json",
        "tsconfig.node.json",
        "index.html",
        "package.json",
    )
    outputs.dir("dist")
    commandLine("pnpm", "run", "build")
}

val frontendDist by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    add("frontendDist", file("dist")) {
        builtBy(pnpmBuild)
    }
}

tasks.named("assemble") {
    dependsOn(pnpmBuild)
}

tasks.named<Delete>("clean") {
    delete("dist", "node_modules")
}
