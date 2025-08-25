dependencies {
    // 공통 모듈 (필수 라이브러리 포함)
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.mysql:mysql-connector-j:9.4.0")
    
    // Test

}

// Spring Boot 메인 클래스 설정
springBoot {
    mainClass.set("me.rgunny.marketpulse.alert.AlertApplication")
}