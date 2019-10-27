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

    private int currentBook = 0;

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
        commands.put("chooseBook", (message -> chooseBook(bot, botState, message)));
        return commands;
    }

    private static HashMap<String,MyFunc> createReadCommands(Bot bot, BotState botState) {
        HashMap<String, MyFunc> commands = new HashMap<>();
        commands.put("/help", (message -> help(bot, message)));
        commands.put("/exitToMain", (message -> exitToMain(bot, botState, message)));
        commands.put("/library", (message -> library(bot, botState, message)));
        commands.put("/infoAboutAuthor", (message -> {
            try {
                getInfoAbAuthor(bot, botState, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("/getThumbnailSketch", (message -> {
            try {
                getThumbnailSketch(bot, botState, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        commands.put("бђ…", (message -> readNext(bot, message)));
        //РїРѕРґСѓРјР°С‚СЊ РЅР°Рґ С‚РµРј, РєР°Рє РїРѕР»СЊР·РѕРІР°С‚РµР»СЊ Р±СѓРґРµС‚ РїРѕР»СѓС‡Р°С‚СЊ РІС‹Р±СЂР°РЅРЅС‹Р№ Р°Р±Р·Р°С†
        return commands;
    }

    //Р’СЃРµРІРѕР·РјРѕР¶РЅС‹Рµ РєРѕРјР°РЅРґС‹ РґР»СЏ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ

    public static void help(Bot bot, Message message){
        bot.printFile("src\\main\\resources\\help.txt", message);
    } // СЃРґРµР»Р°С‚СЊ С„СѓРЅРєС†РёРё РїРµСЂРµС…РѕРґР°. РЅРµ СЃРµР№С‡Р°СЃ

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
        bot.sendMsg(message, "Р’С‹ РІ Р±РёР±Р»РёРѕС‚РµРєРµ.");
        processState(bot, botState);
        bot.printFile("src\\main\\resources\\library.txt", message);
    }

    public static void exitToMain(Bot bot, BotState botState, Message message){
        botState.setState(State.Main);
        processState(bot, botState);
        bot.sendMsg(message, "Р’С‹ РІС‹С€Р»Рё РІ РіР»Р°РІРЅРѕРµ РјРµРЅСЋ.");
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

    public static void getInfoAbAuthor(Bot bot, BotState botState, Message message) throws Exception {
        String site = readFile("src\\main\\resources\\library-authors-wiki-link.txt", botState.currentBook);
        String info = URLReader.GetInfo(site.substring(2), URLReader.InfoAbout.Author);
        System.out.print(info);
        bot.sendMsg(message, info);
    }

    public static void getThumbnailSketch(Bot bot, BotState botState, Message message) throws Exception {
        String site = readFile("src\\main\\resources\\library-wiki-link.txt", botState.currentBook);
        String info = URLReader.GetInfo(site.substring(2), URLReader.InfoAbout.ThumbnailSketchBook);
        System.out.print(info);
        StringBuffer text = new StringBuffer();
        var arInfo = info.toCharArray();
        for (int i = 0; i < arInfo.length; i++){
            if (arInfo[i] == '\n'){
                bot.sendMsg(message, text.toString());
                text = new StringBuffer(); //Р·Р°С‚РѕСЂРјРѕР·РёС‚СЊ РІС‹С…РѕРґ
            }
            else
                text.append(arInfo[i]);
        }

    }

    public static void readNext(Bot bot, Message message){

    } //Р·РґРµСЃСЊ РґРѕРґРµР»Р°С‚СЊ

    public static void chooseBook(Bot bot, BotState botState, Message message){
        if (bot.flChoose){
            botState.setState(State.Read);
            processState(bot, botState);
            bot.sendMsg(message, "РџСЂРёСЏС‚РЅРѕРіРѕ С‡С‚РµРЅРёСЏ"); // РїРѕР·Р¶Рµ СѓРґР°Р»РёС‚СЊ
            botState.currentBook = Integer.parseInt(message.getText());
            //РІС‹РІРµСЃС‚Рё РїРµСЂРІС‹Р№ Р°Р±Р·Р°С†
        }
        bot.flChoose = true;
    }

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
