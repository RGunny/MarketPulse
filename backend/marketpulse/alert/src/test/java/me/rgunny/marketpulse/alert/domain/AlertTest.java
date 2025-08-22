package me.rgunny.marketpulse.alert.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
                Provider.SLACK);
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
}