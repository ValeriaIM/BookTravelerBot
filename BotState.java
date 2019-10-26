import org.telegram.telegrambots.meta.api.objects.Message;

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

    public State GetState(){
        return state;
    }

    public HashMap<String, MyFunc> GetCurrentCommands(){
        return currentCommands;
    }

    public void SetState(State newState){
        state = newState;
    }

    public void SetCurrentCommands(HashMap<String, MyFunc> newCurrentCommands){
        currentCommands = newCurrentCommands;
    }

    public BotState BotState(State newState, HashMap<String, MyFunc> newCurrentCommands){
        var newBotState = new BotState();
        newBotState.SetState(newState);
        newBotState.SetCurrentCommands(newCurrentCommands);
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

    //Всевозможные команды для пользователя

    public static void help(Bot bot, Message message){
        bot.printFile("src\\main\\resources\\help.txt", message);
    } // сделать функции перехода. не сейчас

    public static void authors(Bot bot, Message message) {
        bot.printFile("src\\main\\resources\\authors.txt", message);
    }

    public static void echo(Bot bot, Message message) {
        if (bot.flecho)
            bot.sendMsg(message, message.getText());
        bot.flecho = true;
    }

    public static void printDate(Bot bot, Message message){
        Date date = new Date();
        Locale local = new Locale("ru","RU");
        DateFormat df = DateFormat.getDateTimeInstance (DateFormat.LONG, DateFormat.LONG, local);
        bot.sendMsg(message, df.format(date));
    }

    public static void library(Bot bot, BotState botState, Message message){
        botState.SetState(State.Library);
        bot.sendMsg(message, "Вы в библиотеке.");
        processState(bot, botState);
        bot.printFile("src\\main\\resources\\library.txt", message);
    }

    public static void exitToMain(Bot bot, BotState botState, Message message){
        botState.SetState(State.Main);
        processState(bot, botState);
        bot.printFile("src\\main\\resources\\inMain.txt", message);
    }

    public static void processState(Bot bot, BotState botState){
        switch (botState.GetState()){
            case Main:
                botState.SetCurrentCommands(createPrimitiveCommands(bot, botState));
                break;
            case Library:
                botState.SetCurrentCommands(createLibraryCommands(bot, botState));
                break;
            case Read:
                botState.SetCurrentCommands(createReadCommands(bot, botState));
                break;
        }
    }

    private static HashMap<String,MyFunc> createReadCommands(Bot bot, BotState botState) {
        return new HashMap<>();
    }
}
