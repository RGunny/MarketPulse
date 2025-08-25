import com.google.protobuf.gradle.*

plugins {
    id("com.google.protobuf")
}

dependencies {
    // 공통 모듈 (필수 라이브러리 포함)
    implementation(project(":common"))
    
    // gRPC Server (이벤트 수신)
    implementation("net.devh:grpc-spring-boot-starter:${rootProject.extra["grpcSpringBoot"]}")
    implementation("io.grpc:grpc-protobuf:${rootProject.extra["grpc"]}")
    implementation("io.grpc:grpc-stub:${rootProject.extra["grpc"]}")
    implementation("io.grpc:grpc-netty-shaded:${rootProject.extra["grpc"]}")
    implementation("javax.annotation:javax.annotation-api:${rootProject.extra["javaxAnnotation"]}")
    
    // Slack SDK (알림 발송)
    implementation("com.slack.api:slack-api-client:${rootProject.extra["slack"]}")
    implementation("com.slack.api:slack-api-model:${rootProject.extra["slack"]}")
    
    // Metrics (모니터링)
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // Test
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.grpc:grpc-testing:${rootProject.extra["grpc"]}")
}

// Spring Boot 메인 클래스 설정
springBoot {
    mainClass.set("me.rgunny.marketpulse.notification.NotificationApplication")
}

// Protobuf 설정 - gRPC 서버
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