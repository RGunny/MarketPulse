package me.rgunny.marketpulse.common.infrastructure.config.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB 공통 설정 - MSA 공통 모듈용
 * 
 * Spring Boot 3.x 자동설정을 최대한 활용하면서 필요한 커스터마이징만 추가
 * - 드라이버 레벨 설정 (커넥션 풀, 타임아웃)
 * - _class 필드 제거
 * - 공통 컨버터 설정
 * 
 * 각 서비스 모듈은 이 설정을 상속받아 Repository 스캔 위치만 지정
 */
@Configuration
@ConditionalOnClass(MongoClientSettings.class)
@EnableConfigurationProperties(MongoProperties.class)
public class MongoCommonConfig {

    /**
     * MongoDB 드라이버 레벨 공통 설정
     * Spring Boot가 MongoClient 생성 시 자동으로 적용
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoClientSettingsBuilderCustomizer mongoClientCustomizer(
            @Value("${spring.application.name:marketpulse}") String appName
    ) {
        return builder -> builder
                .applicationName(appName)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .retryWrites(true)
                .applyToConnectionPoolSettings(this::configureConnectionPool)
                .applyToSocketSettings(this::configureSocket);
    }

    /**
     * 커넥션 풀 설정
     */
    private void configureConnectionPool(ConnectionPoolSettings.Builder pool) {
        pool.maxSize(100)
            .minSize(10)
            .maxWaitTime(500, TimeUnit.MILLISECONDS)
            .maxConnectionIdleTime(60, TimeUnit.SECONDS)
            .maxConnectionLifeTime(30, TimeUnit.MINUTES);
    }

    /**
     * 소켓 타임아웃 설정
     */
    private void configureSocket(SocketSettings.Builder socket) {
        socket.connectTimeout(5, TimeUnit.SECONDS)
              .readTimeout(10, TimeUnit.SECONDS);
    }

    /**
     * 커스텀 컨버전 설정 (공통)
     * 각 서비스에서 추가 컨버터가 필요하면 @Primary로 오버라이드
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoCustomConversions customConversions() {
        return MongoCustomConversions.create(config -> {
            // 공통 컨버터 등록
        });
    }

    /**
     * MongoDB 컨버터 커스터마이저 - _class 필드 제거
     * Spring Boot 3.x에서 권장하는 방식
     */
    @Bean
    public MongoConverterCustomizer mongoConverterCustomizer() {
        return converter -> {
            // _class 필드 제거 (타입 정보를 문서에 저장하지 않음)
            converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        };
    }
}