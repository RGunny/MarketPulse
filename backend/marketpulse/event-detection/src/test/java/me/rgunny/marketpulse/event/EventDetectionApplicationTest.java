package me.rgunny.marketpulse.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class EventDetectionApplicationTest {

    @Test
    @DisplayName("EventDetectionApplication run 테스트")
    void runTest() {

        try(MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            EventDetectionApplication.main(new String[0]);

            mocked.verify(() -> SpringApplication.run(EventDetectionApplication.class, new String[0]));
        }
    }
}