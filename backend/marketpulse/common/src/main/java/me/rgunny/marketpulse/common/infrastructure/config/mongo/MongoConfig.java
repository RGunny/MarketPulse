package me.rgunny.marketpulse.common.infrastructure.config.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * MongoDB Reactive 공통 설정
 * 
 * - ReactiveMongoTemplate 빈 생성
 * - _class 필드 제거 설정
 * - Repository 스캔은 각 서비스별로 개별 설정
 */
@Configuration
public class MongoConfig extends AbstractReactiveMongoConfiguration {
 
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        String dbName = connectionString.getDatabase();
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new IllegalStateException("Database name cannot be determined from MongoDB URI: " + mongoUri);
        }
        return dbName;
    }

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected void configureConverters(MongoCustomConversions.MongoConverterConfigurationAdapter adapter) {
        // 커스텀 컨버터 설정 (필요시 확장)
    }

    @Override
    protected boolean autoIndexCreation() {
        return true; // 자동 인덱스 생성 활성화
    }

    /**
     * 애플리케이션 컨텍스트가 준비된 후 _class 필드 제거 설정
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ReactiveMongoTemplate template = event.getApplicationContext().getBean(ReactiveMongoTemplate.class);
        MappingMongoConverter converter = (MappingMongoConverter) template.getConverter();
        // _class 필드 제거 (_class 필드는 MongoDB 문서에 불필요한 타입 정보)
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }
}
