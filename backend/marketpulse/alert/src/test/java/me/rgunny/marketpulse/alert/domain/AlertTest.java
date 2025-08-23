package me.rgunny.marketpulse.alert.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


class AlertTest {

    Alert alert;

    @BeforeEach
    void setUp() {
        alert = Alert.create("marketpulse-sender",
                "slack-receiver",
                "title",
                "content",
                ChannelType.CHAT,
                Provider.SLACK,
                Instant.now());
    }

    @Test
    @DisplayName("Alert 를 신규 생성한다.")
    void createAlert() {
        assertThat(alert.getSender()).isEqualTo("marketpulse-sender");
        assertThat(alert.getReceiver()).isEqualTo("slack-receiver");
        assertThat(alert.getTitle()).isEqualTo("title");
        assertThat(alert.getContent()).isEqualTo("content");
        assertThat(alert.getChannelType()).isEqualTo(ChannelType.CHAT);
        assertThat(alert.getProvider()).isEqualTo(Provider.SLACK);
    }

    @Test
    @DisplayName("Alert 를 notification 으로 발송요청한 뒤 SENT 상태가 된다.")
    void markSent() {
        alert.markSent(Instant.now());

        assertThat(alert.getAlertStatus()).isEqualTo(AlertStatus.SENT);
    }

    @Test
    @DisplayName("notification 에서 발송결과가 성공으로 오면 Alert 가 SUCCEEDED 상태가 된다.")
    void markSucceeded() {
        alert.markSent(Instant.now());

        alert.markSucceeded(Instant.now());

        assertThat(alert.getAlertStatus()).isEqualTo(AlertStatus.SUCCEEDED);
    }

    @Test
    @DisplayName("notification 에서 발송결과가 실패으로 오면 Alert 가 FAILED 상태가 된다.")
    void markFailed() {
        alert.markSent(Instant.now());

        alert.markFailed(Instant.now());

        assertThat(alert.getAlertStatus()).isEqualTo(AlertStatus.FAILED);
    }
}