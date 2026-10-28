plugins {
    id("java")
}

repositories {
    maven { url = uri("https://repo1.maven.org/maven2") }
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
}

tasks.register<JavaExec>("generateDb") {
    group = "database"
    description = "Generate pre-built SQLite database from CSV seed files"
    mainClass.set("DbGenerator")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(
        "${project.projectDir}/src/main/resources/db-seed",
        "${project.projectDir}/../../app/src/main/assets/databases/coil_tester.db"
    )
}