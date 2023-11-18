package com.vs.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final EmailService emailService;

    public void sendNotification(String message) {
        System.out.println(message);
//        try {
//            emailService.sendSimpleEmail("vetrov241201@yandex.ru", "Welcome", "This is a welcome email for your!!");
//        }catch (RuntimeException ex){
//            System.out.println(ex.getMessage());
//        }
    }
}
