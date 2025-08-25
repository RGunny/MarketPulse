// LLM 모듈 - 향후 구현 예정
dependencies {
    // 공통 모듈 (필수 라이브러리 포함)
    implementation(project(":common"))
    
    // LLM 모듈 특화 기능 (향후 구현 예정)
    // - OpenAI API 클라이언트
    // - Claude API 클라이언트
    // - 프롬프트 템플릿 관리
    
    // Test
    testImplementation("io.projectreactor:reactor-test")
}

// Spring Boot 메인 클래스 설정 (향후 구현 시)
// springBoot {
//     mainClass.set("me.rgunny.marketpulse.llm.LlmApplication")
// }