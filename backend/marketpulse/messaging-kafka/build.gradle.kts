// Messaging-Kafka 모듈: Kafka 메시징 라이브러리
dependencies {
    // Spring Kafka (핵심 의존성)
    api("org.springframework.kafka:spring-kafka")
    
    // Spring Boot 기본
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-validation")
    
    // JSON 직렬화
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Test
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:testcontainers:${rootProject.extra["testcontainers"]}")
    testImplementation("org.testcontainers:kafka:${rootProject.extra["testcontainers"]}")
    testImplementation("org.testcontainers:junit-jupiter:${rootProject.extra["testcontainers"]}")
}