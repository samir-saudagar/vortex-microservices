plugins {
	java
	id("org.springframework.boot") version "4.1.0" apply false
	id("io.spring.dependency-management") version "1.1.7"
	id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1" apply false
}

allprojects {
	group = "com.samir"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
		maven("https://packages.confluent.io/maven/")
	}
}

subprojects {
	pluginManager.apply("java")
	pluginManager.apply("org.springframework.boot")
	pluginManager.apply("io.spring.dependency-management")

	configure<JavaPluginExtension> {
		toolchain {
			languageVersion = JavaLanguageVersion.of(25)
		}
	}

	dependencies {
		// --- CORE ---
		add("implementation", "org.springframework.boot:spring-boot-starter-actuator")
		add("implementation", "org.springframework.boot:spring-boot-starter-data-jpa")
		add("implementation", "org.springframework.boot:spring-boot-starter-webmvc")
		add("implementation", "org.springframework.boot:spring-boot-starter-validation")
		add("implementation", "org.springframework.kafka:spring-kafka")
		add("implementation", "com.fasterxml.jackson.core:jackson-databind")
		add("implementation", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

		// --- LOMBOK ---
		add("compileOnly", "org.projectlombok:lombok")
		add("annotationProcessor", "org.projectlombok:lombok")

		// --- DATABASE ---
		add("runtimeOnly", "org.postgresql:postgresql")

		// --- TESTING ---
		add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
		add("testImplementation", "org.springframework.boot:spring-boot-starter-actuator-test")
		add("testImplementation", "org.springframework.kafka:spring-kafka-test")
		add("testCompileOnly", "org.projectlombok:lombok")
		add("testAnnotationProcessor", "org.projectlombok:lombok")
		add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}

project(":flight-info-service") {

	pluginManager.apply("com.github.davidmc24.gradle.plugin.avro")

	dependencies {
		// Resilience4j
		add("implementation", "org.springframework.boot:spring-boot-starter-aop:4.0.0-M2")
		add("implementation", "io.github.resilience4j:resilience4j-spring-boot4:2.4.0")

		// Redis Cache
		add("implementation", "org.springframework.boot:spring-boot-starter-data-redis")

		// Avro & Schema Registry
		add("implementation", "org.apache.avro:avro:1.12.2")
		add("implementation", "io.confluent:kafka-avro-serializer:8.3.1")
	}
}