plugins {
	id("java")
	id("application")
}

group = "org.tsitle.demo_cli_app_critical_path"
version = "1.0-SNAPSHOT"

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
}

tasks.jar {
	manifest {
		attributes["Main-Class"] = "org.tsitle.demo_cli_app_critical_path.Main"
	}
}
