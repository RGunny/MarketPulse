package me.rgunny.marketpulse.unit.config;

import com.mongodb.MongoClientSettings;
import me.rgunny.marketpulse.common.infrastructure.config.mongo.MongoCommonConfig;
import me.rgunny.marketpulse.common.infrastructure.config.mongo.MongoConverterCustomizer;
import org.bson.UuidRepresentation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("MongoDB Common Config Unit 테스트")
class MongoCommonConfigTest {

    @Test
    @DisplayName("MongoDB 클라이언트 설정을 올바르게 커스터마이징한다")
    void givenMongoConfig_whenCustomize_thenAppliesCorrectSettings() {
        // given
        MongoCommonConfig config = new MongoCommonConfig();
        String appName = "test-app";
        
        // when
        MongoClientSettingsBuilderCustomizer customizer = config.mongoClientCustomizer(appName);
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        customizer.customize(builder);
        MongoClientSettings settings = builder.build();
        
        // then
        assertThat(settings.getApplicationName()).isEqualTo(appName);
        assertThat(settings.getUuidRepresentation()).isEqualTo(UuidRepresentation.STANDARD);
        assertThat(settings.getRetryWrites()).isTrue();
        assertThat(settings.getConnectionPoolSettings().getMaxSize()).isEqualTo(100);
        assertThat(settings.getConnectionPoolSettings().getMinSize()).isEqualTo(10);
        assertThat(settings.getConnectionPoolSettings().getMaxWaitTime(TimeUnit.MILLISECONDS)).isEqualTo(500);
        assertThat(settings.getSocketSettings().getConnectTimeout(TimeUnit.SECONDS)).isEqualTo(5);
        assertThat(settings.getSocketSettings().getReadTimeout(TimeUnit.SECONDS)).isEqualTo(10);
    }

    @Test
    @DisplayName("MongoDB 컨버터에서 _class 필드를 제거한다")
    void givenMongoConverter_whenCustomize_thenRemovesClassField() {
        // given
        MongoCommonConfig config = new MongoCommonConfig();
        MappingMongoConverter converter = mock(MappingMongoConverter.class);
        
        // when
        MongoConverterCustomizer customizer = config.mongoConverterCustomizer();
        customizer.convert(converter);
        
        // then
        verify(converter).setTypeMapper(any(DefaultMongoTypeMapper.class));
    }

    @Test
    @DisplayName("커스텀 컨버전 설정이 생성된다")
    void givenMongoConfig_whenCreateCustomConversions_thenReturnsNotNull() {
        // given
        MongoCommonConfig config = new MongoCommonConfig();
        
        // when
        var customConversions = config.customConversions();
        
        // then
        assertThat(customConversions).isNotNull();
    }
}