plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":threadtear-core"))

    implementation("com.github.weisj:darklaf-core") { isChanging = true }
    implementation("com.github.weisj:darklaf-theme") { isChanging = true }
    implementation("com.github.weisj:darklaf-property-loader") { isChanging = true }
    implementation("com.github.weisj:darklaf-extensions-rsyntaxarea")

    implementation("com.fifesoft:rsyntaxtextarea")
    implementation("com.github.jgraph:jgraphx")
    implementation("ch.qos.logback:logback-classic")

    implementation("commons-io:commons-io")
    implementation("org.apache.commons:commons-configuration2")

    implementation(platform("org.lwjgl:lwjgl-bom"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-tinyfd")

    rootProject.extra["lwjgl.natives"].toString().split(",").forEach {
        runtimeOnly("org.lwjgl:lwjgl::$it")
        runtimeOnly("org.lwjgl:lwjgl-tinyfd::$it")
    }
}

tasks.shadowJar {
    archiveBaseName.set(project.name)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes["Main-Class"] = "me.nov.threadtear.Threadtear"
    }
    transform(LicenseTransformer::class.java) {
        destinationPath = "META-INF/licenses/LICENSES.txt"
        include("META-INF/LICENSE", "META-INF/LICENSE.txt")
        exclude("META-INF/THREADTEAR_LICENSE")
    }
    transform(LicenseTransformer::class.java) {
        destinationPath = "META-INF/licenses/NOTICES.txt"
        include("META-INF/NOTICE", "META-INF/NOTICE.txt")
    }
    relocate("META-INF", "META-INF/licenses") {
        includes.addAll(listOf(
            "META-INF/*LICENSE*",
            "META-INF/*NOTICE*",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1"
        ))
        exclude("META-INF/THREADTEAR_LICENSE")
    }
}

tasks.clean {
    doFirst {
        delete(File("$rootDir/dist"))
    }
}

val fatJar by tasks.registering(Copy::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Build a runnable jar with all dependencies"
    dependsOn(tasks.build, tasks.shadowJar)
    destinationDir = File("$rootDir/dist/")
    tasks.shadowJar.flatMap { it.archiveFile }.let {
        val name = it.get().asFile.name
        from(it) {
            include(name)
            rename(name, "threadtear-${project.version}.jar")
        }
    }
}

val runGui by tasks.registering(JavaExec::class) {
    group = "Development"
    description = "Builds and starts Threadtear"
    dependsOn(fatJar)

    jvmArgs = listOf("-noverify")
    workingDir = File(project.rootDir, "dist")
    workingDir.mkdir()
    mainClass.value("me.nov.threadtear.Threadtear")
    classpath("$rootDir/dist/threadtear-${project.version}.jar")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn(tasks.getByPath(":threadtear-core:jar"))

    manifest {
        attributes("Main-Class" to "me.nov.threadtear.Threadtear")
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
