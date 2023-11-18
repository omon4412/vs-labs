package com.vs.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final NotificationService notificationService;
    //private final EmailServiceImpl email
    // ;
    private static final String topicName = "${topic.name}";
    private static final String group = "${spring.kafka.consumer.group-id}";

    @KafkaListener(topics = topicName, groupId = group)
    public void receiveMessage(String message) {
        //email.sendSimpleMessage("vetrov241201@yandex.ru", "Test", message);
        //System.out.println("Received message: " + message);
        try {
            notificationService.sendNotification(message);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
