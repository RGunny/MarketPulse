package me.rgunny.marketpulse.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Notification Service Application
 * - gRPC 기반 이벤트 수신
 * - Slack 알림 발송
 */
@SpringBootApplication(scanBasePackages = "me.rgunny.marketpulse", exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class NotificationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}