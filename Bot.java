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
            try {
                processingMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processingMessage(Message message) throws IOException {
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
        int count = 0;
        for (String command : currentCommands.keySet()) {
            keyboardFirstRow.add(new KeyboardButton(command));
            count++;
            if (count == 3){
                keyboardRowList.add(keyboardFirstRow);
                keyboardFirstRow = new KeyboardRow();
                count = 0;
            }
        }
        if (keyboardFirstRow != null)
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
        commands.put("эхо", (this::echo));
        commands.put("авторы", (this::authors));
        commands.put("дата", (this::printDate));
        commands.put("библиотека", (this::library));
        return commands;
    }

    private HashMap<String, MyFunc> createLibraryCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("?", (this::help));
        commands.put("главное меню", (this::exitToMain));
        commands.put("выбрать книгу", (message -> {
            sendMsg(message, "Введите номер книги");
            chooseBook(message);
        }));
        return commands;
    }

    private HashMap<String, MyFunc> createReadCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("?", (this::help));
        commands.put("библиотека", (this::library));
        commands.put("автор", (message -> {
            try {
                getInfoAbAuthor(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("краткое", (message -> {
            try {
                getThumbnailSketch(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("ᐅ", (this::readNext));
        commands.put("викторина", (this::runQuiz));
        return commands;
    }

    private HashMap<String, MyFunc> createQuizCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("?", (this::help));
        commands.put("библиотека", (this::library));
        commands.put("1", (message -> checkAnswer(message, "1")));
        commands.put("2", (message -> checkAnswer(message, "2")));
        commands.put("3", (message -> checkAnswer(message, "3")));
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
        else if (botState.getCurrentState() == State.state.Quiz)
            printFile("src\\main\\resources\\helpQuiz.txt", message);
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

    private void checkAnswer(Message message, String answer) throws IOException {
        var userDates = getUserDates(message);
        userDates.getCurrentQuiz().setCurrentAnswer(answer);
        runQuiz(message);
    }

    private void runQuiz(Message message) throws IOException {
        var userDates = getUserDates(message);
        if (userDates.getState().getCurrentState() == State.state.Quiz) {
            if(userDates.getCurrentQuiz().getCurrentQuestion() >= userDates.getCurrentQuiz().getAnswers().length){
                sendMsg(message, "Викторина завершена.");
                library(message);
            } else {
                var question = userDates.getCurrentQuiz().getCurrentQuestion();
                var correctAnswer = userDates.getCurrentQuiz().getAnswers()[question];
                if(userDates.getCurrentQuiz().getCurrentAnswer().equals(correctAnswer))
                    sendMsg(message, "Верно");
                else
                    sendMsg(message, "Неверно");
                userDates.getCurrentQuiz().nextQuestion();
                if(userDates.getCurrentQuiz().getCurrentQuestion() < userDates.getCurrentQuiz().getAnswers().length)
                    sendMsg(message, userDates.getCurrentQuiz().getQuestions()[userDates.getCurrentQuiz().getCurrentQuestion()]);
                else {
                    sendMsg(message, "Викторина завершена.");
                    library(message);
                }
            }
        } else{
            userDates.getState().setCurrentState(State.state.Quiz);
            createQuiz(message);
            userDates.setCurrentCommands(createQuizCommands());
            var question = userDates.getCurrentQuiz().getQuestions()[0];
            sendMsg(message, question);
        }
    }

    private void exitToMain(Message message) {
        var userDates = getUserDates(message);
        userDates.getState().setCurrentState(State.state.Main);
        userDates.setCurrentCommands(createPrimitiveCommands());
        sendMsg(message, "Вы вышли в главное меню.");
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

    private void createQuiz(Message message) throws IOException {
        var userDates = getUserDates(message);
        var name = reader.getCurrentBookName(userDates.getCurrentBook());
        var answer = reader.readFile("src\\main\\resources\\quizs\\Answers\\" + name + ".Answers.txt").split(";\n");
        //var arAnswers = googleDrive.getTextByGoogleDisk(googleDrive.getDrive(), name + ".Answers").split("\n");
        var arQuestions = googleDrive.getTextByGoogleDisk(googleDrive.getDrive(), name + ".Questions").split(";");
        userDates.setCurrentQuiz(new Quiz(arQuestions, answer));
    }
}
