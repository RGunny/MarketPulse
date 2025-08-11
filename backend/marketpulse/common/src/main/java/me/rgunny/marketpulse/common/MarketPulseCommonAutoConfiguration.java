package me.rgunny.marketpulse.common;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * MarketPulse Common 모듈 자동 설정
 * 
 * 핵심 공통 기능들을 자동으로 설정하여 다른 모듈에서 쉽게 사용할 수 있도록 함
 * - AOP 로깅 및 응답 처리 (core.aspect)
 * - 전역 예외 처리 (core.exception)
 * - 표준 응답 및 에러코드 (core.response, core.error)
 * - Resilience4j 서킷브레이커 (resilience)
 * - 보안 서비스 (infrastructure.security)
 * 
 * 참고: MongoDB/Redis 설정은 각 모듈별로 개별 설정하여 중복을 방지함
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
    "me.rgunny.marketpulse.common.core",
    "me.rgunny.marketpulse.common.resilience",
    "me.rgunny.marketpulse.common.infrastructure"
})
public class MarketPulseCommonAutoConfiguration {
}