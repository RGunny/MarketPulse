package me.rgunny.event.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * KIS API 관련 설정 외부화
 */
@ConfigurationProperties(prefix = "market-data.kis")
public record KISApiProperties(
        /**
         * KIS API 활성화 여부
         */
        boolean enabled,
        
        /**
         * KIS API 기본 URL
         */
        @NotBlank String baseUrl,
        
        /**
         * KIS API Key (암호화됨)
         */
        @NotBlank String appKey,
        
        /**
         * KIS API Secret (암호화됨)
         */
        @NotBlank String appSecret,
        
        /**
         * 실시간 시세 조회 API 트랜잭션 ID
         */
        @NotBlank String stockPriceTransactionId,
        
        /**
         * OAuth 토큰 발급 시 사용할 grant_type
         */
        @NotBlank String grantType,
        
        /**
         * OAuth 토큰 발급 API 경로
         */
        @NotBlank String tokenPath,
        
        /**
         * 실시간 시세 조회 API 경로
         */
        @NotBlank String stockPricePath,
        
        /**
         * API 연결 검증 경로
         */
        @NotBlank String healthPath,
        
        /**
         * 요청 헤더 설정
         */
        @NotNull Headers headers,
        
        /**
         * 타임아웃 설정
         */
        @NotNull Timeouts timeouts
) {
    
    public KISApiProperties {
        // 기본값 설정
        if (stockPriceTransactionId == null || stockPriceTransactionId.isBlank()) {
            stockPriceTransactionId = "FHKST01010100";
        }
        if (grantType == null || grantType.isBlank()) {
            grantType = "client_credentials";
        }
        if (tokenPath == null || tokenPath.isBlank()) {
            tokenPath = "/oauth2/tokenP";
        }
        if (stockPricePath == null || stockPricePath.isBlank()) {
            stockPricePath = "/uapi/domestic-stock/v1/quotations/inquire-price";
        }
        if (healthPath == null || healthPath.isBlank()) {
            healthPath = "/oauth2/tokenP";
        }
        if (headers == null) {
            headers = new Headers(null, null, null, null, null);
        }
        if (timeouts == null) {
            timeouts = new Timeouts(0, 0, 0, 0);
        }
    }
    
    public record Headers(
            /**
             * 거래ID 헤더명
             */
            @NotBlank String transactionId,
            
            /**
             * 연속거래키 헤더명
             */
            @NotBlank String continuationKey,
            
            /**
             * 커스텀 타입 헤더명
             */
            @NotBlank String custType,
            
            /**
             * 개인고객 타입 값
             */
            @NotBlank String personalCustomerType,
            
            /**
             * Content-Type 헤더 값
             */
            @NotBlank String contentType
    ) {
        public Headers {
            if (transactionId == null || transactionId.isBlank()) {
                transactionId = "tr_id";
            }
            if (continuationKey == null || continuationKey.isBlank()) {
                continuationKey = "tr_cont";
            }
            if (custType == null || custType.isBlank()) {
                custType = "custtype";
            }
            if (personalCustomerType == null || personalCustomerType.isBlank()) {
                personalCustomerType = "P";
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/json; charset=utf-8";
            }
        }
    }
    
    public record Timeouts(
            /**
             * 연결 타임아웃 (초)
             */
            int connectionTimeoutSeconds,
            
            /**
             * 읽기 타임아웃 (초)
             */
            int readTimeoutSeconds,
            
            /**
             * 쓰기 타임아웃 (초)
             */
            int writeTimeoutSeconds,
            
            /**
             * 응답 타임아웃 (초)
             */
            int responseTimeoutSeconds
    ) {
        public Timeouts {
            if (connectionTimeoutSeconds <= 0) connectionTimeoutSeconds = 10;
            if (readTimeoutSeconds <= 0) readTimeoutSeconds = 30;
            if (writeTimeoutSeconds <= 0) writeTimeoutSeconds = 30;
            if (responseTimeoutSeconds <= 0) responseTimeoutSeconds = 60;
        }
    }
}