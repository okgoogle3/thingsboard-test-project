package com.example.MyProject;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import org.springframework.aop.scope.ScopedProxyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewTgBot {
    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot("1964718902:AAGr-1C3k8p0bboOGhvz80AUsHYjEE6ESGE");
        GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
        //List<Update> updates = updatesResponse.updates().stream().filter(update -> update.message().text().equals("/start")).toList();
        List<Update> updates = updatesResponse.updates();
        List<Message> messages = new ArrayList<>();
        updates.forEach(update -> messages.add(update.message()));
        //messages.forEach(message -> System.out.println(message.chat().id()));
        messages.forEach(System.out::println);
    }
}
