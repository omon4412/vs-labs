package com.vs.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
public class NotificationController {
    private final Producer producer;

    @PostMapping
    public String testNotification(@RequestBody @NonNull String message) {
        return producer.sendMessage(message);
    }
}
