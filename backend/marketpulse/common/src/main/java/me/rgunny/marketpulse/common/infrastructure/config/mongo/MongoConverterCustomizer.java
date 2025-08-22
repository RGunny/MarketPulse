package me.rgunny.marketpulse.common.infrastructure.config.mongo;

import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * MongoDB 컨버터 커스터마이저 인터페이스
 */
@FunctionalInterface
public interface MongoConverterCustomizer {
    
    /**
     * MappingMongoConverter 커스터마이징
     * 
     * @param converter MongoDB 컨버터
     */
    void convert(MappingMongoConverter converter);
}