package com.example.MyProject.bot;

import com.example.MyProject.model.SubscriberModel;
import com.example.MyProject.repo.SubscriberRepo;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Getter
@Setter
public class TgBot {
    private Logger logger = LogManager.getLogger(TgBot.class);
    public static TelegramBot bot = new TelegramBot("6116276576:AAEurzwvN0eUyps8aGomnw8Xwn6Tqwp78Sk");
    public SubscriberRepo subscriberRepo;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    @Autowired
    public TgBot(SubscriberRepo subscriberRepo) {
        this.subscriberRepo = subscriberRepo;
        initialization();
    }

    public void initialization() {
        bot.setUpdatesListener(updates -> {
            updates.removeIf(element -> element.message() == null || element.message().text() == null);
            Long id;
            for (Update update : updates) {
                id = update.message().chat().id();
                logger.trace("New message from "+ update.message().chat().username());
                if(update.message().text().equals("/subscribe")) {
                    if (subscriberRepo.existsById(id)) bot.execute(new SendMessage(id, "You are a subscriber."));
                    else {
                        subscriberRepo.save(new SubscriberModel(id));
                        bot.execute(new SendMessage(id, "You have subscribed on notifications!"));
                    }
                }
                if(update.message().text().equals("/unsubscribe")) {
                    if (subscriberRepo.existsById(id)) {
                        subscriberRepo.delete(subscriberRepo.findById(id).orElseThrow());
                        bot.execute(new SendMessage(id, "You have unsubscribed from notifications."));
                    }
                    else bot.execute(new SendMessage(id, "You are not a subscriber."));
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendTemperatureMessage (String deviceName, Double temperature){
        try{
            executor.execute(() -> subscriberRepo.findAll().forEach(user -> bot.execute(new SendMessage(user.getId(), "Device "
                    + deviceName + " is getting hot!\nCurrent temperature "
                    + String.format(Locale.US,"%.2f", temperature)  + " degrees Celsius"))));
        }catch (Exception ignored){

        }
    }

    public void sendLeavingPerimeterMessage (String deviceName) {
        try{
            executor.execute(() -> subscriberRepo.findAll().forEach(user -> bot.execute(new SendMessage(user.getId(), "Device "
                    + deviceName + " leaving perimeter"))));
        }catch (Exception ignored){
        }
    }

}

/*private Logger logger = LogManager.getLogger(TgBot.class);
    public static TelegramBot bot = new TelegramBot("6116276576:AAEurzwvN0eUyps8aGomnw8Xwn6Tqwp78Sk");
    public SubscriberRepo subscriberRepo;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    @Autowired
    public TgBot(SubscriberRepo subscriberRepo) {
        this.subscriberRepo = subscriberRepo;
    }

    public void initialization() {
        bot.setUpdatesListener(updates -> {
            updates.removeIf(element -> element.message() == null || element.message().text() == null);
            Long id;
            for (Update update : updates) {
                id = update.message().chat().id();
                logger.trace("New message from "+ update.message().chat().username());
                if(update.message().text().equals("/subscribe")) {
                    if (subscriberRepo.existsById(id)) bot.execute(new SendMessage(id, "You are a subscriber."));
                    else {
                        subscriberRepo.save(new SubscriberModel(id));
                        bot.execute(new SendMessage(id, "You have subscribed on notifications!"));
                    }
                }
                if(update.message().text().equals("/unsubscribe")) {
                    if (subscriberRepo.existsById(id)) {
                        subscriberRepo.delete(subscriberRepo.findById(id).orElseThrow());
                        bot.execute(new SendMessage(id, "You have unsubscribed from notifications."));
                    }
                    else bot.execute(new SendMessage(id, "You are not a subscriber."));
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendTemperatureMessage (String deviceName, Double temperature){
        try{
            executor.execute(() -> subscriberRepo.findAll().forEach(id -> bot.execute(new SendMessage(id, "Device "
                    + deviceName + " is getting hot!\nCurrent temperature "
                    + String.format(Locale.US,"%.2f", temperature)  + " degrees Celsius"))));
        }catch (Exception ignored){

        }
    }

    public void sendLeavingPerimeterMessage (String deviceName) {
        try{
            executor.execute(() -> subscriberRepo.findAll().forEach(id -> bot.execute(new SendMessage(id, "Device "
                    + deviceName + " leaving perimeter"))));
        }catch (Exception ignored){
        }
    }
    */