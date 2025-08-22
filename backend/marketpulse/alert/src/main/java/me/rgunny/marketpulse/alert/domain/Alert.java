package me.rgunny.marketpulse.alert.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.rgunny.marketpulse.alert.domain.shared.AbstractEntity;

import java.time.LocalDateTime;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    static Alert create(String sender, String receiver, String title, String content, ChannelType channelType, Provider provider) {
        Alert alert = new Alert();
        alert.sender = sender;
        alert.receiver = receiver;
        alert.title = title;
        alert.content = content;
        alert.channelType = channelType;
        alert.provider = provider;
        alert.createdAt = LocalDateTime.now();
        alert.updatedAt = LocalDateTime.now();

        return alert;
    }


}
