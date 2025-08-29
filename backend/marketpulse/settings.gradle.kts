rootProject.name = "marketpulse"

// 공통 라이브러리 모듈
include("common")

// 메시징 인프라 모듈
include("messaging-kafka")

// 비즈니스 도메인 모듈
include("event-detection")
include("alert")
include("notification")
include("watchlist")
// include("llm")  // 아직 미구현

// 향후 추가 예정 모듈
// include("market")
// include("screening")
// include("member")