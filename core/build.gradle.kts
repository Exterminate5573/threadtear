plugins {
    `java-library`
}

fun DependencyHandlerScope.externalLib(libraryName: String) {
    implementation(files("${rootProject.rootDir}/libs/$libraryName.jar"))
}

dependencies {
    implementation("commons-io:commons-io")

    implementation("org.apache.commons:commons-configuration2")
    implementation("commons-beanutils:commons-beanutils")

    api("org.ow2.asm:asm-tree")
    implementation("org.ow2.asm:asm")
    implementation("org.ow2.asm:asm-analysis")
    implementation("org.ow2.asm:asm-util")
    implementation("org.ow2.asm:asm-commons")
    implementation("com.github.Col-E:CAFED00D")

    implementation("com.github.leibnitz27:cfr:0.152") { isChanging = true }
    implementation("org.quiltmc:quiltflower")
    implementation("ch.qos.logback:logback-classic")

    implementation("net.fabricmc:mapping-io")

    //Procyon
    implementation("org.bitbucket.mstrobel:procyon-core:0.6.0")
    implementation("org.bitbucket.mstrobel:procyon-expressions:0.6.0")
    implementation("org.bitbucket.mstrobel:procyon-reflection:0.6.0")
    implementation("org.bitbucket.mstrobel:procyon-compilertools:0.6.0")

    //externalLib("fernflower-13-12-22")
}
