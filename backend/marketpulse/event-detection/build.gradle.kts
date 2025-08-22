import com.google.protobuf.gradle.*

plugins {
    id("com.google.protobuf")
}

dependencies {
    // 공통 모듈 (필수 라이브러리 포함)
    implementation(project(":common"))
    
    // gRPC Client (알림 서비스 통신)
    implementation("net.devh:grpc-client-spring-boot-starter:${rootProject.extra["grpcSpringBoot"]}")
    implementation("io.grpc:grpc-protobuf:${rootProject.extra["grpc"]}")
    implementation("io.grpc:grpc-stub:${rootProject.extra["grpc"]}")
    implementation("io.grpc:grpc-netty-shaded:${rootProject.extra["grpc"]}")
    implementation("javax.annotation:javax.annotation-api:${rootProject.extra["javaxAnnotation"]}")

    // 테스트
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:testcontainers:${rootProject.extra["testcontainers"]}")
    testImplementation("org.testcontainers:junit-jupiter:${rootProject.extra["testcontainers"]}")
    testImplementation("org.testcontainers:mongodb:${rootProject.extra["testcontainers"]}")
    testImplementation("com.squareup.okhttp3:mockwebserver:${rootProject.extra["mockwebserver"]}")
}

// Spring Boot 메인 클래스 설정
springBoot {
    mainClass.set("me.rgunny.marketpulse.event.EventDetectionApplication")
}

// Protobuf 설정 - gRPC 클라이언트
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${rootProject.extra["protobuf"]}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${rootProject.extra["grpc"]}"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}