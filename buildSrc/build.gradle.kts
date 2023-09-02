plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.github.johnrengelman:shadow:8.1.1")
    implementation("org.codehaus.plexus:plexus-utils:4.0.0")
    implementation("org.apache.ant:ant:1.10.13")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
