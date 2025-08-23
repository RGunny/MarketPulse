package me.rgunny.marketpulse.alert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.rgunny.marketpulse.alert.domain.shared.AbstractEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

import static org.springframework.util.Assert.state;

@Entity
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Alert extends AbstractEntity {

    private String sender;

    private String receiver;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private AlertStatus alertStatus;

    @Enumerated(EnumType.STRING)
    private ChannelType channelType;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public static Alert create(String sender, String receiver, String title, String content, ChannelType channelType, Provider provider, Instant now) {
        Alert alert = new Alert();
        alert.sender = sender;
        alert.receiver = receiver;
        alert.title = title;
        alert.content = content;
        alert.alertStatus = AlertStatus.PENDING;
        alert.channelType = channelType;
        alert.provider = provider;
        alert.createdAt = now;
        alert.updatedAt = now;

        return alert;
    }

    public void markSent(Instant now) {
        state(alertStatus == AlertStatus.PENDING, "PENDING 상태가 아닙니다.");

        this.alertStatus = AlertStatus.SENT;
        this.updatedAt = now;
    }

    public void markSucceeded(Instant now) {
        state(alertStatus == AlertStatus.SENT, "SENT 상태가 아닙니다.");

        this.alertStatus = AlertStatus.SUCCEEDED;
        this.updatedAt = now;
    }

    public void markFailed(Instant now) {
        state(alertStatus == AlertStatus.SENT, "SENT 상태가 아닙니다.");

        this.alertStatus = AlertStatus.FAILED;
        this.updatedAt = now;
    }

}
