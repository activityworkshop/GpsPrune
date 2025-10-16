plugins {
    application
}

group = "tim.prune"
version = 26.1

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    sourceSets["main"].apply {
        java.srcDir("src")
        resources.srcDirs("src")
        resources.include(
                "tim/prune/gui/images/**",
                "tim/prune/lang/*",
                "tim/prune/function/srtm/*.dat",
                "tim/prune/*.txt"
        )
    }

    sourceSets["test"].apply {
        java.srcDir("test")
        resources.srcDirs("test")
        resources.include(
            "tim/prune/function/weather/xml/*",
            "tim/prune/function/filesleuth/data/*.txt",
            "tim/prune/gui/map/*.png"
        )
        output.setResourcesDir(layout.buildDirectory.dir("classes/java/test"))
    }
}

application {
    mainClass.set("$group.GpsPrune")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.scijava.org/content/repositories/public/") }
}

dependencies {
    val j3dVersion = "1.5.2"
    implementation("java3d:j3d-core:$j3dVersion")
    implementation("java3d:vecmath:$j3dVersion")
    implementation("java3d:j3d-core-utils:$j3dVersion")
    val jUnitVersion = "5.7.1"
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Main-Class"] = "${project.group}.GpsPrune"
    }
}

tasks.withType(Test::class) {
    useJUnitPlatform()
}
