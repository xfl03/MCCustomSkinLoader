plugins {
    id("java")
}

group = "customskinloader"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("com.qcloud:cos_api:5.6.98")
    implementation(gradleApi())
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("commons-io:commons-io:2.11.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}