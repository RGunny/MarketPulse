package me.rgunny.marketpulse.event.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Clock;

/**
 * 테스트용 시간 설정
 * 
 * 테스트 환경에서 Clock Bean을 오버라이드하여 재현 가능한 테스트 환경 제공
 * 
 * 사용법:
 * - @Import(TestTimeConfig.class) 또는
 * - @SpringBootTest(classes = {YourConfig.class, TestTimeConfig.class})
 */
@TestConfiguration
public class TestTimeConfig {
    
    /**
     * 테스트용 고정 Clock
     * TestClockFactory를 활용하여 일관된 테스트 시간 제공
     */
    @Bean
    @Primary
    @Profile("test")
    public Clock testClock() {
        // 기본값: 월요일 장중 시간
        // 다른 시간이 필요한 경우 개별 테스트에서 @TestConfiguration으로 오버라이드
        return TestClockFactory.marketMiddle();
    }
}