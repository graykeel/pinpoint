plugins {
    id("com.navercorp.pinpoint.java8-library")
}

dependencies {
    api(project(":pinpoint-commons"))
    api(project(":pinpoint-bootstrap-core"))
    implementation(libs.log4j.api)
    implementation(libs.junit)
}

description = "pinpoint-plugin-it-jdbc-test"
