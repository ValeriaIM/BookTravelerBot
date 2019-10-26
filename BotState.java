import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class BotState {

    enum State{
        Main,
        Library,
        Read
    }

    private State state = State.Main;
    private HashMap<String, MyFunc> currentCommands;

    public State getState(){
        return state;
    }

    public HashMap<String, MyFunc> getCurrentCommands(){
        return currentCommands;
    }

    public void setState(State newState){
        state = newState;
    }

    public void setCurrentCommands(HashMap<String, MyFunc> newCurrentCommands){
        currentCommands = newCurrentCommands;
    }


    public BotState BotState(State newState, HashMap<String, MyFunc> newCurrentCommands){
        var newBotState = new BotState();
        newBotState.setState(newState);
        newBotState.setCurrentCommands(newCurrentCommands);
        return newBotState;
    }


    public static HashMap<String, MyFunc> createPrimitiveCommands(Bot bot, BotState botState) {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("/help", (message -> help(bot, message)));
        commands.put("/echo", (message -> echo(bot, message)));
        commands.put("/authors", (message -> authors(bot, message)));
        commands.put("/printDate", (message -> printDate(bot, message)));
        commands.put("/library", (message -> library(bot, botState, message)));
        return commands;
    }

    public static HashMap<String, MyFunc> createLibraryCommands(Bot bot, BotState botState) {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("/help", (message -> help(bot, message)));
        commands.put("/exitToMain", (message -> exitToMain(bot, botState, message)));
        return commands;
    }

    private static HashMap<String,MyFunc> createReadCommands(Bot bot, BotState botState) {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("/help", (message -> help(bot, message)));
        commands.put("/exitToMain", (message -> exitToMain(bot, botState, message)));
        commands.put("/library", (message -> library(bot, botState, message)));
        commands.put("/infoAboutAuthor", (message -> {
            try {
                getInfoAbAuthor(bot, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("getThumbnailSketch", (message -> {
            try {
                getThumbnailSketch(bot, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("ᐅ", (message -> readNext(bot, message)));
        //подумать над тем, как пользователь будет получать выбранный абзац
        return commands;
    }

    //Всевозможные команды для пользователя

    public static void help(Bot bot, Message message){
        bot.printFile("src\\main\\resources\\help.txt", message);
    } // сделать функции перехода. не сейчас

    public static void authors(Bot bot, Message message) {
        bot.printFile("src\\main\\resources\\authors.txt", message);
    }

    public static void echo(Bot bot, Message message) {
        if (bot.flEcho)
            bot.sendMsg(message, message.getText());
        bot.flEcho = true;
    }

    public static void printDate(Bot bot, Message message){
        Date date = new Date();
        Locale local = new Locale("ru","RU");
        DateFormat df = DateFormat.getDateTimeInstance (DateFormat.LONG, DateFormat.LONG, local);
        bot.sendMsg(message, df.format(date));
    }

    public static void library(Bot bot, BotState botState, Message message){
        botState.setState(State.Library);
        bot.sendMsg(message, "Вы в библиотеке.");
        processState(bot, botState);
        bot.printFile("src\\main\\resources\\library.txt", message);
    }

    public static void exitToMain(Bot bot, BotState botState, Message message){
        botState.setState(State.Main);
        processState(bot, botState);
        bot.sendMsg(message, "Вы вышли в главное меню.");
    }

    public static void processState(Bot bot, BotState botState){
        switch (botState.getState()){
            case Main:
                botState.setCurrentCommands(createPrimitiveCommands(bot, botState));
                break;
            case Library:
                botState.setCurrentCommands(createLibraryCommands(bot, botState));
                break;
            case Read:
                botState.setCurrentCommands(createReadCommands(bot, botState));
                break;
        }
    }

    public static void getInfoAbAuthor(Bot bot, Message message) throws Exception {
        String site = readFile("src\\main\\resources\\library-authors-wiki-link.txt", Integer.parseInt(message.getText()));
        String info = URLReader.GetInfo(site, URLReader.InfoAbout.Author);
        bot.sendMsg(message, info);
    }

    public static void getThumbnailSketch(Bot bot, Message message) throws Exception {
        String site = readFile("src\\main\\resources\\library-wiki-link.txt", Integer.parseInt(message.getText()));
        String info = URLReader.GetInfo(site, URLReader.InfoAbout.ThumbnailSketchBook);
        bot.sendMsg(message, info);
    }

    public static void readNext(Bot bot, Message message){

    } //здесь доделать

    public static void chooseBook(Bot bot, Message message){
        if (bot.flChoose)
            //bot.sendMsg(message, message.getText());
        bot.flChoose = true;
    } //здесь доделать
    
    private static String readFile(String nameFile, int n){
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
            return line;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
