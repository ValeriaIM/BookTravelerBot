import org.o7planning.googledrive.quickstart.GoogleDrive;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.util.*;

public class Bot extends BotPrimitive {
    private HashMap<String, UserDates> Users = new HashMap<>();
    private Reader reader = new Reader();
    private GoogleDrive googleDrive;

    {
        try {
            googleDrive = new GoogleDrive();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotApi = new TelegramBotsApi();
        try {
            Bot bot = new Bot();
            telegramBotApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) { //прием сообщений, получение обновлений, реализованный на лонгпул - запрос
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            processingMessage(message);
        }
    }

    private void processingMessage(Message message){
        var chatId = message.getChatId().toString();
        if (!Users.containsKey(chatId)) {
            addNewUser(chatId);
        }
        var currentCommands = getUserCommands(message);
        var userDates = getUserDates(message);
        if (userDates.getFlEcho()) {
            echo(message);
            userDates.setFlEcho(false);
        } else if (userDates.getFlChoose()) {
            chooseBook(message);
            userDates.setFlChoose(false);
        } else if (currentCommands.containsKey(message.getText())) {
            MyFunc func = currentCommands.get(message.getText());
            func.func(message);
        } else
            sendMsg(message, "Неверная команда, попробуй еще раз.");
    }

    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = getSendMessage(message, text);
        try {
            addButtons(sendMessage, message);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addButtons(SendMessage sendMessage, Message message) {
        var currentCommands = getUserCommands(message);
        var replyKeyboardMarkup = getReplyKeyboardMarkup();//инициализация клавиатуры
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        for (String command : currentCommands.keySet()) {
            keyboardFirstRow.add(new KeyboardButton(command));
        }
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    private void printFile(String nameFile, Message message) {
        sendMsg(message, reader.readFile(nameFile));
    }

    private void setCurrentParagraphsList(UserDates userDates) {
        try {
            var bookName = reader.getCurrentBookName(userDates.getCurrentBook());
            userDates.setCurrentParagraphsList(googleDrive.getParagraphsList(googleDrive.getTextByGoogleDisk(googleDrive.getDrive(), bookName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////
    private State getUserState(Message message){
        String chatId = message.getChatId().toString();
        if (Users.containsKey(chatId))
            return Users.get(chatId).getState();
        addNewUser(chatId);
        return Users.get(chatId).getState();
    }

    private HashMap<String, MyFunc> getUserCommands(Message message){
        String chatId = message.getChatId().toString();
        if (Users.containsKey(chatId))
            return Users.get(chatId).getCurrentCommands();
        addNewUser(chatId);
        return Users.get(chatId).getCurrentCommands();
    }

    private UserDates getUserDates(Message message){
        String chatId = message.getChatId().toString();
        if (Users.containsKey(chatId))
            return Users.get(chatId);
        addNewUser(chatId);
        return Users.get(chatId);
    }

    private void addNewUser(String chatId){
        var dates = new UserDates();
        dates.setCurrentCommands(createPrimitiveCommands());
        Users.put(chatId, dates);
    }

    //////////////////
    private HashMap<String, MyFunc> createPrimitiveCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("?", (this::help));
        commands.put("echo", (this::echo));
        commands.put("authors", (this::authors));
        commands.put("printDate", (this::printDate));
        commands.put("library", (this::library));
        return commands;
    }

    private HashMap<String, MyFunc> createLibraryCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("?", (this::help));
        commands.put("exitToMain", (this::exitToMain));
        commands.put("chooseBook", (message -> {
            sendMsg(message, "Введите номер книги");
            chooseBook(message);
        }));
        return commands;
    }

    private HashMap<String, MyFunc> createReadCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("?", (this::help));
        // commands.put("/exitToMain", (message -> exitToMain(bot, botState, message)));
        commands.put("library", (this::library));
        commands.put("infoAboutAuthor", (message -> {
            try {
                getInfoAbAuthor(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("getThumbnailSketch", (message -> {
            try {
                getThumbnailSketch(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("ᐅ", (this::readNext));
        return commands;
    }

    private HashMap<String, MyFunc> createQuizCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("?", (this::help));
        commands.put("exitToLibrary", (this::exitToLibrary));
        commands.put("1", (this::checkAnswer));
        commands.put("2", (this::checkAnswer));
        commands.put("3", (this::checkAnswer));
        commands.put("4", (this::checkAnswer));
        return commands;
    }
    //////////////////
    private void help(Message message) {
        var botState = getUserState(message);
        if (botState.getCurrentState() == State.state.Main)
            printFile("src\\main\\resources\\helpMain.txt", message);
        else if (botState.getCurrentState() == State.state.Library)
            printFile("src\\main\\resources\\helpLibrary.txt", message);
        else if (botState.getCurrentState() == State.state.Read)
            printFile("src\\main\\resources\\helpRead.txt", message);
    } // сделать функции перехода. не сейчас

    private void authors(Message message) {
        printFile("src\\main\\resources\\authors.txt", message);
    }

    private void echo(Message message) {
        var userDates = getUserDates(message);
        if (userDates.getFlEcho())
            sendMsg(message, message.getText());
        userDates.setFlEcho(true);
    }

    private void printDate(Message message) {
        Date date = new Date();
        Locale local = new Locale("ru", "RU");
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, local);
        sendMsg(message, df.format(date));
    }

    private void library(Message message) {
        var userDates = getUserDates(message);
        userDates.getState().setCurrentState(State.state.Library);
        userDates.setCurrentCommands(createLibraryCommands());
        sendMsg(message, "Вы в библиотеке.");
        printFile("src\\main\\resources\\library.txt", message);
    }

    private void exitToMain(Message message) {
        var userDates = getUserDates(message);
        userDates.getState().setCurrentState(State.state.Main);
        userDates.setCurrentCommands(createPrimitiveCommands());
        sendMsg(message, "Вы вышли в главное меню.");
    }
    
    private void exitToLibrary(Message message) {
        var userDates = getUserDates(message);
        userDates.getState().setCurrentState(State.state.Library);
        userDates.setCurrentCommands(createLibraryCommands());
        sendMsg(message, "Вы вышли в библиотеку.");
    }

    private void chooseBook(Message message) {
        var userDates = getUserDates(message);
        var countBookInLibrary = reader.getCountLinesInFile("src\\main\\resources\\library.txt");
        if (userDates.getFlChoose()) {
            userDates.getState().setCurrentState(State.state.Read);
            userDates.setCurrentCommands(createReadCommands());
            var number = Integer.parseInt(message.getText());
            if (number < 0 || countBookInLibrary < number) {
                sendMsg(message, "Неправильно выбран номер книги");
                userDates.getState().setCurrentState(State.state.Library);
                userDates.setCurrentCommands(createLibraryCommands());
            } else {
                sendMsg(message, "Приятного чтения");
                userDates.setCurrentBook(Integer.parseInt(message.getText()));
                setCurrentParagraphsList(userDates);
            }
        }
        userDates.setFlChoose(true);
    }

    private void getInfoAbAuthor(Message message) throws Exception {
        var book = getUserDates(message).getCurrentBook();
        String site = reader.readFileLine("src\\main\\resources\\library-authors-wiki-link.txt", book);
        String info = URLReader.GetInfo(site.substring(2), URLReader.InfoAbout.Author);
        sendMsg(message, info);
    }

    private void getThumbnailSketch(Message message) throws Exception {
        var book = getUserDates(message).getCurrentBook();
        String site = reader.readFileLine("src\\main\\resources\\library-wiki-link.txt", book);
        String info = URLReader.GetInfo(site.substring(2), URLReader.InfoAbout.ThumbnailSketchBook);
        String[] lines = info.split("\r\n|\r|\n");
        for (String line : lines){
            sendMsg(message, line);
        }
    }

    private void readNext(Message message) {
        var userDates = getUserDates(message);
        var pos = userDates.getCurrentPosition();
        sendMsg(message, userDates.getCurrentParagraphsList().get(pos));
        userDates.setCurrentPosition(pos + 1);
    }
    
    private void checkAnswer(Message message) {
        
    }
}
