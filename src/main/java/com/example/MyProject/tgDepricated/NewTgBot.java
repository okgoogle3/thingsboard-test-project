package com.example.MyProject.tgDepricated;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import org.springframework.aop.scope.ScopedProxyUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NewTgBot {
    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot("");
        GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
        //List<Update> updates = updatesResponse.updates().stream().filter(update -> update.message().text().equals("/start")).toList();
        List<Update> updates = updatesResponse.updates();
        Set<Long> chat_id = new HashSet<>();
        updates.removeIf(element -> element.message() == null);
        updates.forEach(update -> chat_id.add(update.message().chat().id()));

        chat_id.forEach(System.out::println);
        //messages.forEach(message -> System.out.println(message.chat().id()));
        //messages.forEach(message -> System.out.println(message));
        //messages.forEach(System.out::println);
    }
}
