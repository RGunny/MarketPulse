package me.rgunny.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
},
        //TODO: common 모듈 AutoConfiguration으로 리팩토링
        scanBasePackages = {
                "me.rgunny.event",
                "me.rgunny.marketpulse.common"
        })
public class EventDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventDetectionApplication.class, args);
    }
}
