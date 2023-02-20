package com.example.MyProject.bot;

import com.example.MyProject.service.AssetService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Getter
@Setter
public class TgBot {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public AssetService assetService;

    public static TelegramBot bot = new TelegramBot("6116276576:AAEurzwvN0eUyps8aGomnw8Xwn6Tqwp78Sk");
    public Set<Long> chat_id = new HashSet<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();


    @PostConstruct
    public void initialization() {
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
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendTemperatureMessage (String deviceName, Double temperature){
        executor.execute(() -> chat_id.forEach(id -> bot.execute(new SendMessage(id, "Device "
                + deviceName + " is getting hot!\nCurrent temperature "
                + temperature + " degrees Celsius"))));
    }

    public void sendLeavingPerimeterMessage (String deviceName) {
        executor.execute(() -> chat_id.forEach(id -> bot.execute(new SendMessage(id, "Device "
                + deviceName + " leaving perimeter"))));
    }

}
