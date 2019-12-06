import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bot extends BotPrimitive {
    private BotLogic botLogic = new BotLogic();
    private BotCommands botCommands = new BotCommands();

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
        if (!botLogic.getUsers().containsKey(chatId)) {
            botLogic.getUsers().put(chatId, createUser(chatId));
        }
        var currentCommands = botLogic.getUserCommands(chatId, this);
        var userDates = botLogic.getUserData(chatId, this);
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
        if (text == "")
            return;
        SendMessage sendMessage = getSendMessage(message, text);
        try {
            addButtons(sendMessage, message);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addButtons(SendMessage sendMessage, Message message) {
        var currentCommands = botLogic.getUserCommands(message.getChatId().toString(), this);
        var replyKeyboardMarkup = getReplyKeyboardMarkup();//инициализация клавиатуры
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        int count = 0;
        for (String command : currentCommands.keySet()) {
            keyboardFirstRow.add(new KeyboardButton(command));
            count++;
            if (count == 3) {
                keyboardRowList.add(keyboardFirstRow);
                keyboardFirstRow = new KeyboardRow();
                count = 0;
            }
        }
        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    private void printFile(String nameFile, Message message) {
        sendMsg(message, botLogic.getReader().readFile(nameFile));
    }

    public UserData createUser(String chatId) {
        var data = new UserData();
        data.setCurrentCommands(createPrimitiveCommands());
        return data;
    }

    ////////////////
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
    } //createCommands
    ///////////////
    private void help(Message message) {
        sendMsg(message, botCommands.help(message, botLogic, this));
    } // сделать функции перехода. не сейчас

    private void authors(Message message) {
        printFile("src\\main\\resources\\authors.txt", message);
    }

    private void echo(Message message) {
        var userDates = botLogic.getUserData(message.getChatId().toString(), this);
        if (userDates.getFlEcho())
            sendMsg(message, message.getText());
        userDates.setFlEcho(true);
    }

    private void printDate(Message message) {
        sendMsg(message, botCommands.getDate());
    }

    private void library(Message message) {
        sendMsg(message, botCommands.library(message, botLogic, this));
        botLogic.getUserData(message.getChatId().toString(), this).setCurrentCommands(createLibraryCommands());
        sendMsg(message, "Вы в библиотеке.");
        printFile("src\\main\\resources\\library.txt", message);
    }

    private void checkAnswer(Message message, String answer) throws IOException {
        var userDates = botLogic.getUserData(message.getChatId().toString(), this);
        userDates.getCurrentQuiz().setCurrentAnswer(answer);
        runQuiz(message);
    }

    private void runQuiz(Message message) throws IOException {
        var userData = botLogic.getUserData(message.getChatId().toString(), this);
        if (userData.getState().getCurrentState() == State.state.Quiz) {
            var quantity = userData.getCurrentQuiz().getAnswers().length;
            var n = userData.getCurrentQuiz().getCurrentQuestion();
            var correctAnswer = userData.getCurrentQuiz().getAnswers()[n];
            if (userData.getCurrentQuiz().getCurrentAnswer().equals(correctAnswer)) {
                sendMsg(message, "Верно");
                userData.getCurrentQuiz().addCorrectAnswers();
            } else
                sendMsg(message, "Неверно");
            userData.getCurrentQuiz().nextQuestion();
            if (userData.getCurrentQuiz().getCurrentQuestion() < userData.getCurrentQuiz().getAnswers().length)
                sendMsg(message, botCommands.getNextQuestion(userData, quantity));
            else
                library(message);
        } else {
            userData.setCurrentCommands(createQuizCommands());
            sendMsg(message, botCommands.handleFirstEntry(userData, botLogic, message.getChatId().toString()));
        }
    }

    private void exitToMain(Message message) {
        var userDates = botLogic.getUserData(message.getChatId().toString(), this);
        userDates.getState().setCurrentState(State.state.Main);
        userDates.setCurrentCommands(createPrimitiveCommands());
        sendMsg(message, "Вы вышли в главное меню.");
    }

    private void chooseBook(Message message) {
        var userData = botLogic.getUserData(message.getChatId().toString(), this);
        var text = botCommands.chooseBook(message, botLogic, userData);
        if (text.equals("Неправильно выбран номер книги"))
            userData.setCurrentCommands(createLibraryCommands());
        else
            userData.setCurrentCommands(createReadCommands());
        sendMsg(message, text);
    }

    private void getInfoAbAuthor(Message message) throws Exception {
        sendMsg(message, botCommands.getInfoAbAuthor(message.getChatId().toString(), botLogic, this));
    }

    private void getThumbnailSketch(Message message) throws Exception {
        String[] lines = botCommands.getThumbnailSketch(message.getChatId().toString(), botLogic, this);
        for (String line : lines) {
            sendMsg(message, line);
        }
    }

    private void readNext(Message message) {
        var userDates = botLogic.getUserData(message.getChatId().toString(), this);
        var pos = userDates.getCurrentPosition();
        sendMsg(message, userDates.getCurrentParagraphsList().get(pos));
        userDates.setCurrentPosition(pos + 1);
    } //commandsWithSendMessage
}
