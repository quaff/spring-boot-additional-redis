plugins {
	java
	id("org.springframework.boot").version("3.4.0")
	id("io.spring.dependency-management").version("latest.release")
	id("io.spring.javaformat").version("latest.release")
}

java {
	version = 21
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

tasks.withType<Test>() {
	useJUnitPlatform()
	jvmArgs(listOf("-javaagent:${mockitoAgent.asPath}", "-Xshare:off"))
}
