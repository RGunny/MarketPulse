package me.rgunny.marketpulse.unit.config;

import me.rgunny.marketpulse.common.config.MongoConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MongoDB Config Unit 테스트")
class MongoConfigTest {

    @Test
    @DisplayName("MongoDB URI에서 데이터베이스 이름을 추출할 수 있다")
    void givenMongoUri_whenGetDatabaseName_thenExtractsCorrectly() {
        // given
        MongoConfig mongoConfig = new MongoConfig();
        String testUri = "mongodb://testuser:testpass@localhost:27017/testdb?authSource=testdb";
        ReflectionTestUtils.setField(mongoConfig, "mongoUri", testUri);

        // when - protected 메서드이므로 리플렉션 사용
        String databaseName = ReflectionTestUtils.invokeMethod(mongoConfig, "getDatabaseName");

        // then
        assertThat(databaseName).isEqualTo("testdb");
    }

    @Test
    @DisplayName("쿼리 파라미터가 있는 URI에서도 데이터베이스 이름을 추출할 수 있다")
    void givenMongoUriWithQueryParams_whenGetDatabaseName_thenExtractsCorrectly() {
        // given
        MongoConfig mongoConfig = new MongoConfig();
        String uriWithParams = "mongodb://localhost:27017/mydb?authSource=admin&readPreference=primary";
        ReflectionTestUtils.setField(mongoConfig, "mongoUri", uriWithParams);

        // when
        String databaseName = ReflectionTestUtils.invokeMethod(mongoConfig, "getDatabaseName");

        // then
        assertThat(databaseName).isEqualTo("mydb");
    }

    @Test
    @DisplayName("쿼리 파라미터가 없는 URI에서도 데이터베이스 이름을 추출할 수 있다")
    void givenMongoUriWithoutQueryParams_whenGetDatabaseName_thenExtractsCorrectly() {
        // given
        MongoConfig mongoConfig = new MongoConfig();
        String simpleUri = "mongodb://localhost:27017/simpledb";
        ReflectionTestUtils.setField(mongoConfig, "mongoUri", simpleUri);

        // when
        String databaseName = ReflectionTestUtils.invokeMethod(mongoConfig, "getDatabaseName");

        // then
        assertThat(databaseName).isEqualTo("simpledb");
    }
}
