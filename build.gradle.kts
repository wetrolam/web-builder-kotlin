plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
}

group = "net.useobjects"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.10")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
    implementation("org.yaml:snakeyaml:1.25") // TODO upgrade to version 2.4 or higher (code change is needed)

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
