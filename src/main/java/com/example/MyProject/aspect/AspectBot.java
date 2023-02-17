package com.example.MyProject.aspect;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Aspect
@Component
@Getter
@Setter
public class AspectBot {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static TelegramBot bot = new TelegramBot("6116276576:AAEurzwvN0eUyps8aGomnw8Xwn6Tqwp78Sk");
    public static Set<Long> chat_id = new HashSet<>();

    @PostConstruct
    public void initialization() {
        /*GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(0);
        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
        //List<Update> updates = updatesResponse.updates().stream().filter(update -> update.message().text().equals("/start")).toList();
        List<Update> updates = updatesResponse.updates();*/

        bot.setUpdatesListener(updates -> {
            updates.removeIf(element -> element.message() == null || element.message().text() == null);
            Long id;
            for (Update update : updates) {
                id = update.message().chat().id();
                logger.info("New message from "+ update.message().chat().username());
                if(update.message().text().equals("/subscribe")) {
                    if (chat_id.contains(id)) bot.execute(new SendMessage(id, "You are a subscriber."));
                    else {
                        chat_id.add(id);
                        bot.execute(new SendMessage(id, "You have subscribed on notifications!"));
                    }
                }
                if(update.message().text().equals("/unsubscribe")) {
                    if (chat_id.contains(id)) {
                        chat_id.remove(id);
                        bot.execute(new SendMessage(id, "You have unsubscribed from notifications."));
                    }
                    else bot.execute(new SendMessage(id, "You are not a subscriber."));
                }
            }
            //updates.forEach(update -> chat_id.add(update.message().chat().id()));
            //System.out.println(updates);
            //System.out.println(chat_id);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    @Pointcut("execution(public * com.example.MyProject.controller.DeviceController.*(..)) || execution(public * com.example.MyProject.controller.AssetController.*(..))")
    public void callAtMyServicePublic() { }

    @After("callAtMyServicePublic()")
    public void afterCallAt() {
        //chat_id.parallelStream().forEach(id -> bot.execute(new SendMessage(id, "Aspect is working rn"))); //for future improvements
        //chat_id.forEach(id -> bot.execute(new SendMessage(id, "Aspect is working rn")));
    }
}
