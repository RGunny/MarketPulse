package me.rgunny.marketpulse.common.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = {"me.rgunny.marketpulse", "me.rgunny.event"})
public class MongoConfig extends AbstractReactiveMongoConfiguration {
 
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        // URI에서 데이터베이스 이름 추출
        String[] parts = mongoUri.split("/");
        String dbPart = parts[parts.length - 1];
        return dbPart.contains("?") ? dbPart.substring(0, dbPart.indexOf("?")) : dbPart;
    }

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() throws Exception {
        ReactiveMongoTemplate template = new ReactiveMongoTemplate(
                reactiveMongoClient(), 
                getDatabaseName()
        );
        
        // _class 필드 제거 설정
        MappingMongoConverter converter = (MappingMongoConverter) template.getConverter();
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        
        return template;
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(java.util.Collections.emptyList());
    }
}
