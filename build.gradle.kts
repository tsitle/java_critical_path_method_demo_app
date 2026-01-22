plugins {
	id("java")
	id("application")
	id("org.beryx.jlink") version "3.2.0"
}

group = "org.tsitle.demo_cli_app_critical_path"
version = "1.0"

val APP_NAME = "cpm_demo"

repositories {
	mavenCentral()
	flatDir { dirs("../lib-critical_path_method/build/libs") }
}

dependencies {
	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	//implementation("org.jspecify:jspecify:1.0.0")  // transitive dependency of io.github.tsitle.criticalpath
	implementation("com.google.guava:guava:33.5.0-jre")  // for HTML escaping
	implementation("com.google.code.gson:gson:2.13.2")  // for JSON deserialization
	implementation("info.picocli:picocli:4.7.7")  // for CLI arguments parsing
	implementation(":lib_critical_path_method:1.0")
}

java {
	modularity.inferModulePath.set(true)
}

tasks.test {
	useJUnitPlatform()
}

application {
	mainClass = "org.tsitle.demo_cli_app_critical_path.Main"
	mainModule = "org.tsitle.demo_cli_app_critical_path"
}

tasks.jar {
	manifest {
		attributes["Main-Class"] = "org.tsitle.demo_cli_app_critical_path.Main"
		attributes["Implementation-Title"] = APP_NAME
		attributes["Implementation-Version"] = version
	}
}

jlink {
	// name of the launcher script produced in the launcher image
	launcher {
		name = APP_NAME
	}

	// output directory and ZIP filename for the launcher image
	imageDir = File(layout.buildDirectory.get().toString(), "${APP_NAME}-${version}")
	imageZip = File(layout.buildDirectory.get().toString(), "${APP_NAME}-${version}.zip")

	// reduce the size of the launcher image
	options.set(listOf("--strip-debug", "--compress", "zip-6", "--no-header-files", "--no-man-pages"))
}
