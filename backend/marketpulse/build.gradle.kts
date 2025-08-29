plugins {
    java
    id("org.springframework.boot") version "3.5.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

allprojects {
    group = "me.rgunny.marketpulse"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

// 버전 카탈로그 - 실제 사용되는 버전만 유지
object Versions {
    const val springBoot = "3.5.5"
    const val grpc = "1.63.0"  // grpc-spring-boot-starter와 호환되는 버전
    const val grpcSpringBoot = "3.1.0.RELEASE"
    const val protobuf = "3.25.1"  // 최신 버전으로 업데이트
    const val testcontainers = "1.20.1"  // 최신 버전으로 업데이트
    const val resilience4j = "2.2.0"
    const val slack = "1.40.3"
    const val javaxAnnotation = "1.3.2"
    const val mockwebserver = "4.12.0"
}

// extra properties for subprojects
extra["grpc"] = Versions.grpc
extra["grpcSpringBoot"] = Versions.grpcSpringBoot
extra["protobuf"] = Versions.protobuf
extra["testcontainers"] = Versions.testcontainers
extra["resilience4j"] = Versions.resilience4j
extra["slack"] = Versions.slack
extra["javaxAnnotation"] = Versions.javaxAnnotation
extra["mockwebserver"] = Versions.mockwebserver

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
    
    // 모듈 타입별 플러그인 적용
    when (name) {
        "common", "messaging-kafka" -> {
            apply(plugin = "java-library")
        }
        "event-detection", "notification", "alert", "watchlist" -> {
            apply(plugin = "org.springframework.boot")
        }
    }
    
    // Spring Boot BOM 적용
    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${Versions.springBoot}")
        }
    }

    dependencies {
        // Lombok - 모든 모듈 공통 (Java 21 호환)
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        
        // Test - 모든 모듈 공통
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }
}

// Gradle Wrapper 설정
tasks.wrapper {
    gradleVersion = "8.14.3"
    distributionType = Wrapper.DistributionType.BIN
}
