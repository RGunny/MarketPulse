dependencies {
    // 공통 모듈 (필수 라이브러리 포함)
    implementation(project(":common"))
    
    // Alert 모듈 특화 기능 (현재는 common에서 모두 제공)
    // 향후 alert 전용 기능 추가 시 여기에 명시
    
    // Test
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:mongodb:${rootProject.extra["testcontainers"]}")
}

// Spring Boot 메인 클래스 설정
springBoot {
    mainClass.set("me.rgunny.marketpulse.alert.AlertApplication")
}