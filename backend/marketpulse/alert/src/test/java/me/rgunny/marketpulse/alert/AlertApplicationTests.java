package me.rgunny.marketpulse.alert;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class AlertApplicationTests {

    @Test
    void contextLoads() {
        // 컨텍스트 로딩 테스트
        try(MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            AlertApplication.main(new String[0]);

            mocked.verify(() -> SpringApplication.run(AlertApplication.class, new String[0]));
        }
    }

}
