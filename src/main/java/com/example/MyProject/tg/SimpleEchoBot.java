package com.example.MyProject.tg;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SimpleEchoBot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
    }

    @Override
    public String getBotUsername() {
        return "кирил";
    }

    @Override
    public String getBotToken() {
        return "1964718902:AAGr-1C3k8p0bboOGhvz80AUsHYjEE6ESGE";
    }
}