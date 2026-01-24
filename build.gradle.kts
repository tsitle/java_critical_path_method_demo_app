plugins {
	id("java")
	id("application")
	id("org.beryx.jlink") version "3.2.0"
	id("com.google.osdetector") version "1.7.3"  // see https://github.com/google/osdetector-gradle-plugin
}

group = "org.tsitle.demo_cli_app_critical_path"
version = "1.0"

val propProjName = "cpm_demo"

// ---------------------------------------------------------------------------------------------------------------------

fun getOperatingSystemName() : String {
	return if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
		"macos"
	} else if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
		"linux"
	} else if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
		"win"
	} else {
		throw Error("Operating System not supported")
	}
}

fun getCpuArchitecture() : String {
	return when (System.getProperty("os.arch")) {
		"x86_64", "x64", "amd64" -> "x64"
		/*Linux/macOS:*/"aarch64" -> "aarch64"
		else -> throw Error("CPU Architecture not supported")
	}
}

fun getLinuxDistroType() : String {
	if (getOperatingSystemName() != "linux") {
		return "none"
	}
	val tmpRel = osdetector.release
	if (tmpRel.isLike("debian")) {
		return "debian"
	}
	if (tmpRel.isLike("redhat") || tmpRel.isLike("fedora")) {
		return "redhat"
	}
	throw Error("Linux distribution type '${tmpRel}' not supported")
}

val osName: String = getOperatingSystemName()
val cpuArch: String = getCpuArchitecture()
if (osName == "win" && cpuArch != "x64") {
	throw Error("Cannot build for ${osName}-${cpuArch}")
}
val lxDistroType: String = getLinuxDistroType()

println("Host: ${osName}-${cpuArch}")

// ---------------------------------------------------------------------------------------------------------------------

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	//implementation("org.jspecify:jspecify:1.0.0")  // transitive dependency of io.github.tsitle.criticalpath
	implementation("com.google.guava:guava:33.5.0-jre")  // for HTML escaping
	implementation("com.google.code.gson:gson:2.13.2")  // for JSON deserialization
	implementation("info.picocli:picocli:4.7.7")  // for CLI arguments parsing
	implementation("io.github.tsitle:criticalpath:1.0")
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
		// the following two attributes are required in order for the '--version' CLI option to work
		attributes["Implementation-Title"] = propProjName
		attributes["Implementation-Version"] = version
	}
}

jlink {
	// name of the launcher script produced in the launcher image
	launcher {
		name = propProjName
	}

	// output directory and ZIP filename for the launcher image
	imageDir = File(layout.buildDirectory.get().toString(), "${propProjName}-${osName}-${cpuArch}-${version}")
	imageZip = File(layout.buildDirectory.get().toString(), "${propProjName}-${osName}-${cpuArch}-${version}.zip")

	// reduce the size of the launcher image
	options.set(listOf("--strip-debug", "--compress", "zip-6", "--no-header-files", "--no-man-pages"))
}
