import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Bot extends TelegramLongPollingBot {
    public static void main(String[] args){
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotApi = new TelegramBotsApi();
        try {
            telegramBotApi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) { //прием сообщений, получение обновлений, реализованный на лонгпул - запрос
        Message message = update.getMessage();
        if (message != null && message.hasText()){
            switch (message.getText()){
                case ("/help"):
                    help(message);
                    break;
                case ("/echo"):
                    echo(message); // здесь надо доработать
                    break;
                case ("/authors"):
                    authors(message);
                    break;
                case ("/printDate"):
                    printDate(message);
                    break;
                case ("/library"):
                    library(message);
                    break;
            }
        }
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        try {
            setButtom(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() { // Вернуть имя бота при регистрации
        return "BookTraveler";
    }

    public String getBotToken() { // Вернуть полученный токен
        return "741739778:AAFfKTQkbLQkePnWPJaRhe11uAJFnUYcfaM";
    }

    public void setButtom(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();//инициализация клавиатуры
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);//не скрывать клаву

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();

        keyboardFirstRow.add(new KeyboardButton("/help"));
        keyboardFirstRow.add(new KeyboardButton("/echo"));
        keyboardFirstRow.add(new KeyboardButton("/authors"));
        keyboardFirstRow.add(new KeyboardButton("/printDate"));
        keyboardFirstRow.add(new KeyboardButton("/library"));

        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public void printSmallFile(String nameFile, Message message) {
        try{
            File file = new java.io.File(nameFile);
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String line = reader.readLine();
            StringBuffer text = new StringBuffer();

            while (line != null) {
                text.append(line + "\n");
                line = reader.readLine();
            }
            sendMsg(message, text.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // выводит все целиком, для больших файлов другой сделаем. он юудет по частям.

    public void help(Message message){
        printSmallFile("src\\main\\resources\\help.txt", message);
    } // сделать функции перехода. не сейчас

    public void authors(Message message) {
        printSmallFile("src\\main\\resources\\authors.txt", message);
    }

    public void echo(Message message) {
        sendMsg(message, message.getText());
    }

    public void printDate(Message message){
        Date date = new Date();
        Locale local = new Locale("ru","RU");
        DateFormat df = DateFormat.getDateTimeInstance (DateFormat.LONG, DateFormat.LONG, local);
        sendMsg(message, df.format(date));
    }

    public void library(Message message){
        printSmallFile("src\\main\\resources\\library.txt", message);
    }
}
