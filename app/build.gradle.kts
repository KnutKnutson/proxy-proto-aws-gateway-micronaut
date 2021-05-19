plugins {
    kotlin("jvm").version("1.4.21")
    kotlin("kapt").version("1.4.21")
    application
}

repositories {
    mavenCentral()
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
    named<Test>("test") {
        useJUnitPlatform()
    }
}

dependencies {
    implementation(platform(kotlin("bom")))

    // AWS
    implementation("com.amazonaws:aws-lambda-java-events:2.2.7")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")

    // Micronaut
    val micronautVersion = "2.5.1"
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation("io.micronaut.aws:micronaut-function-aws-api-proxy")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.grpc:micronaut-protobuff-support")

    implementation("javax.annotation:javax.annotation-api")
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut.security:micronaut-security")
    kapt("io.micronaut:micronaut-validation")

    // Serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")

    // Testing
    // Micronaut system test dependencies
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("io.micronaut:micronaut-http-server-netty")
    testImplementation("io.micronaut.test:micronaut-test-kotlintest")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    kaptTest("io.micronaut:micronaut-bom:$micronautVersion")
    kaptTest("io.micronaut:micronaut-inject-java")

    // junit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0")
}

application {
    // Define the main class for the application.
    mainClass.set("micronaut.proxy.proto.AppKt")
}
