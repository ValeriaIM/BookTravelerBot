import org.o7planning.googledrive.quickstart.GoogleDrive;
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
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.util.*;

interface MyFunc{
    void func(Message message);
}
public class Bot extends TelegramLongPollingBot {
    public Boolean flChoose = false;
    public Boolean flEcho = false;

    enum State{
        Main,
        Library,
        Read
    }

    private State botState = State.Main;
    public HashMap<String, MyFunc> currentCommands = createPrimitiveCommands();
    private GoogleDrive googleDrive;
    {
        try {
            googleDrive = new GoogleDrive();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private int countBookInLibrary = 2;
    private int currentBook = 0;
    private ArrayList<String> currentParagraphsList = new ArrayList<>();
    private int currentPosition = 0;

    public static void main(String[] args){
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

        if (message != null && message.hasText()){
            processingMessage(message);
        }
    }

    public void processingMessage(Message message) throws IOException {
        if (flEcho) {
            echo(message);
            flEcho = false;
        }
        else if (flChoose) {
            chooseBook(message);
            flChoose = false;
        }
        else if (currentCommands.containsKey(message.getText())){
            MyFunc func = currentCommands.get(message.getText());
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

        for (String command : currentCommands.keySet()) {
            keyboardFirstRow.add(new KeyboardButton(command));
        }

        keyboardRowList.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public void updateButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();//инициализация клавиатуры
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);//не скрывать клаву

        List<KeyboardRow> keyboardRowList = new ArrayList<KeyboardRow>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();

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

    public HashMap<String, MyFunc> createPrimitiveCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("/help", (message -> help(message)));
        commands.put("/echo", (message -> echo(message)));
        commands.put("/authors", (message -> authors(message)));
        commands.put("/printDate", (message -> printDate(message)));
        commands.put("/library", (message -> library(message)));
        return commands;
    }

    public HashMap<String, MyFunc> createLibraryCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("/help", (message -> help(message)));
        commands.put("/exitToMain", (message -> exitToMain(message)));
        commands.put("chooseBook", (message -> {
            sendMsg(message, "Введите номер книги");
            chooseBook(message);}));
        return commands;
    }

    private HashMap<String,MyFunc> createReadCommands() {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("/help", (message -> help(message)));
        // commands.put("/exitToMain", (message -> exitToMain(bot, botState, message)));
        commands.put("/library", (message -> library(message)));
        commands.put("/infoAboutAuthor", (message -> {
            try {
                getInfoAbAuthor(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("/getThumbnailSketch", (message -> {
            try {
                getThumbnailSketch(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("ᐅ", (message -> {

            readNext(message);

        }));
        //подумать над тем, как пользователь будет получать выбранный абзац
        return commands;
    }

    public void help(Message message){
        if(botState == State.Main)
            printFile("src\\main\\resources\\helpMain.txt", message);
        else if(botState == State.Library)
            printFile("src\\main\\resources\\helpLibrary.txt", message);
        else if (botState == State.Read)
            printFile("src\\main\\resources\\helpRead.txt", message);


    } // сделать функции перехода. не сейчас

    public void authors(Message message) {
        printFile("src\\main\\resources\\authors.txt", message);
    }

    public void echo(Message message) {
        if (flEcho)
            sendMsg(message, message.getText());
        flEcho = true;
    }

    public void printDate(Message message){
        Date date = new Date();
        Locale local = new Locale("ru","RU");
        DateFormat df = DateFormat.getDateTimeInstance (DateFormat.LONG, DateFormat.LONG, local);
        sendMsg(message, df.format(date));
    }

    public void library(Message message){
        botState = State.Library;
        currentCommands = createLibraryCommands();
        sendMsg(message, "Вы в библиотеке.");
        printFile("src\\main\\resources\\library.txt", message);
    }

    public void exitToMain(Message message){
        botState = State.Main;
        currentCommands = createPrimitiveCommands();
        sendMsg(message, "Вы вышли в главное меню.");
    }

    public void chooseBook(Message message){
        if (flChoose){
            botState = State.Read;
            currentCommands = createReadCommands();
            var number = Integer.parseInt(message.getText());
            if(number < 0 || countBookInLibrary < number){
                sendMsg(message, "Неправильно выбран номер книги");
                botState = State.Library;
                currentCommands = createLibraryCommands();
            } else {
                sendMsg(message, "Приятного чтения");
                currentBook = Integer.parseInt(message.getText());
                setCurrentParagraphsList();
                sendMsg(message, currentParagraphsList.get(currentPosition)); //выводит первый абзац
                currentPosition++;
            }
        }
        flChoose = true;
    }

    public void getInfoAbAuthor(Message message) throws Exception {
        String site = readFileLine("src\\main\\resources\\library-authors-wiki-link.txt", currentBook);
        String info = URLReader.GetInfo(site.substring(2), URLReader.InfoAbout.Author);
        sendMsg(message, info);
    }

    public void getThumbnailSketch(Message message) throws Exception {
        String site = readFileLine("src\\main\\resources\\library-wiki-link.txt", currentBook);
        String info = URLReader.GetInfo(site.substring(2), URLReader.InfoAbout.ThumbnailSketchBook);
        StringBuffer text = new StringBuffer();
        var arInfo = info.toCharArray();
        for (int i = 0; i < arInfo.length; i++){
            if (arInfo[i] == '\n'){
                sendMsg(message, text.toString());
                text = new StringBuffer(); //затормозить выход
            }
            else
                text.append(arInfo[i]);
        }
    }

    private String readFileLine(String nameFile, int n){
        try {
            File file = new java.io.File(nameFile);
            FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(fileReader);
            int count = 1;
            String line = reader.readLine();
            while ((line != null) && (count < n)) {
                line = reader.readLine();
                count++;
            }
            reader.close();
            return line;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void readNext(Message message){
        sendMsg(message, currentParagraphsList.get(currentPosition));
        currentPosition++;
    }
    
    private String getCurrentBookName() {
        var result = new StringBuffer();
        var name = readFileLine("library.txt", currentBook).toCharArray();
        var fl= false;
        for(var i =0; i < name.length; i++){
            if (fl)
                result.append(name[i]);
            if(name[i] == '"')
                fl = !fl;
        }
        return result.deleteCharAt(result.length() - 1).toString();
    }

    private void setCurrentParagraphsList(){
        try {
            currentParagraphsList = googleDrive.getParagraphsList(googleDrive.getTextByGoogleDisk(googleDrive.getDrive(), getCurrentBookName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
