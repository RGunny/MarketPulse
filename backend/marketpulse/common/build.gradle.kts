// Common 모듈: 공통 라이브러리 및 설정 제공
dependencies {
    // Spring Boot 기본 (필수)
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.springframework.boot:spring-boot-starter-web")

    // WebFlux (MSA 기본 - Reactive)
    api("org.springframework.boot:spring-boot-starter-webflux")
    
    // Jasypt for encryption
    api("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")
    
    // Data Access - MongoDB & Redis (NoSQL 기반)
    api("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    api("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    
    // Resilience4j (서킷브레이커)
    api("io.github.resilience4j:resilience4j-spring-boot3:${rootProject.extra["resilience4j"]}")
    api("io.github.resilience4j:resilience4j-reactor:${rootProject.extra["resilience4j"]}")
    
    // Monitoring
    api("org.springframework.boot:spring-boot-starter-actuator")
    
    // Test
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:testcontainers:${rootProject.extra["testcontainers"]}")
    testImplementation("org.testcontainers:mongodb:${rootProject.extra["testcontainers"]}")
}