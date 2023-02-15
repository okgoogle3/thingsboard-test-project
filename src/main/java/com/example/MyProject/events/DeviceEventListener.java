package com.example.MyProject.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DeviceEventListener {
    @Async
    @EventListener
    public void sendMsgEvent(String message) {
        System.out.println("==EmailListener 2 ===" + message);
    }
}