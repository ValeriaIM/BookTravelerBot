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
import java.nio.charset.StandardCharsets;
import java.util.*;

interface MyFunc{
    void func(Message message);
}

public class Bot extends TelegramLongPollingBot {
    public Boolean flEcho = false;
    public Boolean flChoose = false;

    public static BotState botState = new BotState();

    public static void main(String[] args){
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotApi = new TelegramBotsApi();
        try {
            Bot bot = new Bot();
            telegramBotApi.registerBot(bot);
            botState.setCurrentCommands(BotState.createPrimitiveCommands(bot, botState));
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) { //прием сообщений, получение обновлений, реализованный на лонгпул - запрос
        Message message = update.getMessage();

        if (message != null && message.hasText()){
            processingMessage(message);
        }
    }

    public void processingMessage(Message message){
        var commands = botState.getCurrentCommands();
        if ((flEcho) || (flChoose)){
            if (flEcho) {
                BotState.echo(this, message);
                flEcho = false;
            }
            else {
                BotState.chooseBook(this, botState, message);
                flChoose = false;
            }
        }
        else
            if (commands.containsKey(message.getText())){
                MyFunc func = commands.get(message.getText());
                func.func(message);
            }
            else
                sendMsg(message, "Неверная команда, попробуй еще раз.");
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        try {
            addButtons(sendMessage);
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

    public void addButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();//инициализация клавиатуры
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);//не скрывать клаву

        List<KeyboardRow> keyboardRowList = new ArrayList<KeyboardRow>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        
        var currentCommands = botState.getCurrentCommands();
        for (String command : currentCommands.keySet()) {
            keyboardFirstRow.add(new KeyboardButton(command));
        }

        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public void printFile(String nameFile, Message message) {
        try{
            File file = new java.io.File(nameFile);
            FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
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
    }
}
