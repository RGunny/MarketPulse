# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🎯 프로젝트 개요

**프로젝트**: 실시간 금융 시세 감지 및 알림 시스템 (MarketPulse)  
**목적**: 백엔드 개발자 이직용 실무 포트폴리오  
**목표 수준**: 네이버, 카카오, 토스 등 국내 IT 대기업 실무 수준

### 핵심 기술 스택
- **Java 21** + **Spring Boot 3.2** (최신 LTS)
- **Spring WebFlux** (Reactive Programming)
- **DDD** (Domain-Driven Design)
- **헥사고날 아키텍처** (Ports & Adapters)
- **함수형 프로그래밍** (Record, Stream API, Optional)
- **gRPC** (마이크로서비스 통신)
- **MongoDB** + **Redis** (NoSQL)

## 📁 프로젝트 구조

```
MarketPulse/
├── backend/marketpulse/              # 백엔드 소스
│   ├── common/                       # 공통 모듈
│   ├── event-detection/              # 이벤트 감지 모듈
│   └── notification/                 # 알림 모듈
└── infrastructure/                   # 인프라 설정
    └── local/docker-compose.yml     # MongoDB, Redis
```

## 🏗️ 헥사고날 아키텍처

```
├── domain/                  # 순수 도메인 (프레임워크 독립)
│   ├── model/              # 엔티티, 값 객체
│   └── service/            # 도메인 서비스
├── application/            # 애플리케이션 계층
│   ├── port/in/           # 유스케이스 인터페이스
│   ├── port/out/          # 외부 시스템 포트
│   └── service/           # 유스케이스 구현
└── infrastructure/        # 인프라 계층
    ├── adapter/in/        # REST, gRPC, Scheduler
    ├── adapter/out/       # DB, API, Cache
    └── config/            # 설정 클래스
```

## 📊 주요 도메인

### Event-Detection 모듈
- **Stock**: 종목 마스터 데이터
- **StockPrice**: 실시간 시세 (TTL 24h)
- **MarketRanking**: 시장 순위 (상승/하락/거래량)
- **AlertHistory**: 알림 이력 (쿨다운 관리)
- **WatchTarget**: 감시 종목 (CORE/THEME/MOMENTUM)

### Notification 모듈
- **gRPC Server**: 포트 9082
- **Slack 연동**: Webhook 기반 알림

## 💻 개발 환경

### 필수 환경변수
```bash
export KIS_APP_KEY=your_app_key
export KIS_APP_SECRET=your_app_secret
export SLACK_WEBHOOK_URL=your_webhook_url
export JASYPT_ENCRYPTOR_PASSWORD=your_password
```

### 실행 명령
```bash
# 인프라 실행
cd infrastructure/local
docker-compose up -d

# 애플리케이션 실행
./gradlew :event-detection:bootRun   # 포트 8081
./gradlew :notification:bootRun      # 포트 8082

# 테스트 실행
./gradlew test
```

## 🧪 테스트 전략

### BDD 스타일
```java
@Test
@DisplayName("캐시에 데이터가 있으면 캐시에서 조회한다")
void given_cacheHasData_when_getCurrentPrice_then_returnFromCache() {
    // given - 사전 조건
    // when - 실행
    // then - 검증
}
```

### 테스트 구조
- `unit/`: 단위 테스트 (Mock)
- `medium/`: 통합 테스트 (Testcontainers)
- `integration/`: E2E 테스트

## 📈 최근 추가 기능 (2025.08.19)

### ✅ MarketRanking 도메인
- KIS API 실시간 순위 조회
- 상승률/하락률/거래량 상위 30위
- 자동 WatchTarget 등록 (상위 10위)
- 10분 주기 스케줄러 수집
- MongoDB 7일 데이터 보관

### ✅ 자동 초기화
- StockDataInitializer: 16개 주요 종목
- WatchTargetInitializer: 감시 대상 설정

## 🎯 코드 품질 원칙

### 1. 도메인 중심 설계
- 불변 객체 (Record, final)
- 팩토리 메서드 패턴
- 비즈니스 로직 캡슐화

### 2. 함수형 프로그래밍
- Stream API 활용
- Optional 체이닝
- 순수 함수 지향

### 3. 예외 처리
```java
// ✅ ErrorCode 기반 예외
throw new BusinessException(ErrorCode.INVALID_SYMBOL);
```

### 4. 의존성 주입
- 생성자 주입 (@RequiredArgsConstructor)
- 인터페이스 기반 설계

## 📝 Git 컨벤션
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 코드 개선
- `test`: 테스트 추가
- `docs`: 문서 수정
- `chore`: 빌드, 설정

## 🚧 TODO
- [ ] 테스트 커버리지 80% 달성
- [ ] API 문서화 (OpenAPI 3.0)
- [ ] 성능 모니터링 (Micrometer)
- [ ] 배치 API 최적화
- [ ] 공휴일 API 연동

---

**Important**: 이 문서는 프로젝트의 Single Source of Truth입니다.
모든 코드는 여기 명시된 원칙을 따라야 합니다.