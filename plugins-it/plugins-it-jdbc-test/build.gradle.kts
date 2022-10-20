plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-bootstrap-core"))
    implementation(libs.log4j.api)
    implementation(libs.junit)
    implementation("org.testcontainers:testcontainers:1.17.2")
    implementation("org.testcontainers:jdbc:1.17.2")
}

description = "pinpoint-plugin-it-jdbc-test"
