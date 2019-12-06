import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class BotCommands {
    public String help(Message message, BotLogic botLogic, Bot bot) {
        String chatId = message.getChatId().toString();
        var botState = botLogic.getUserState(chatId, bot);
        if (botState.getCurrentState() == State.state.Main)
            return botLogic.getReader().readFile("src\\main\\resources\\helpMain.txt");
        else if (botState.getCurrentState() == State.state.Library)
            return botLogic.getReader().readFile("src\\main\\resources\\helpLibrary.txt");
        else if (botState.getCurrentState() == State.state.Read)
            return botLogic.getReader().readFile("src\\main\\resources\\helpRead.txt");
        else if (botState.getCurrentState() == State.state.Quiz)
            return botLogic.getReader().readFile("src\\main\\resources\\helpQuiz.txt");
        return "";
    } // сделать функции перехода. не сейчас

    public String getDate() {
        Date date = new Date();
        Locale local = new Locale("ru", "RU");
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, local);
        return df.format(date);
    }

    public String library(Message message, BotLogic botLogic, Bot bot) {
        var userDates = botLogic.getUserData(message.getChatId().toString(), bot);
        if (userDates.getState().getCurrentState() == State.state.Quiz) {
            var quantity = userDates.getCurrentQuiz().getAnswers().length;
            var text = "Викторина завершена. Количество правильных ответов: " + userDates.getCurrentQuiz().getCorrectAnswers() + "/" + quantity;
            return text;
        }
        userDates.getState().setCurrentState(State.state.Library);
        return "";
    }

    public String getInfoAbAuthor(String chatId, BotLogic botLogic, Bot bot) throws Exception {
        var book = botLogic.getUserData(chatId, bot).getCurrentBook();
        String site = botLogic.getReader().readFileLine("src\\main\\resources\\library-authors-wiki-link.txt", book);
        String info = URLReader.GetInfo(site.substring(2), URLReader.InfoAbout.Author);
        return info;
    }

    public String[] getThumbnailSketch(String chatId, BotLogic botLogic, Bot bot) throws Exception {
        var book = botLogic.getUserData(chatId, bot).getCurrentBook();
        String site = botLogic.getReader().readFileLine("src\\main\\resources\\library-wiki-link.txt", book);
        String info = URLReader.GetInfo(site.substring(2), URLReader.InfoAbout.ThumbnailSketchBook);
        return info.split("\r\n|\r|\n");
    }

    public String handleFirstEntry(UserData userData, BotLogic botLogic, String chatId) throws IOException {
        userData.getState().setCurrentState(State.state.Quiz);
        botLogic.createQuiz(chatId);
        var question = userData.getCurrentQuiz().getQuestions()[0];
        var quantity = userData.getCurrentQuiz().getAnswers().length;
        return "1/" + quantity + ". " + question;
    }

    public String getNextQuestion(UserData userData, int quantity) {
        var n = userData.getCurrentQuiz().getCurrentQuestion();
        var question = userData.getCurrentQuiz().getQuestions()[n].substring(2);
        n++;
        return n + "/" + quantity + ". " + question;
    }

    public String chooseBook(Message message, BotLogic botLogic, UserData userData) {
        var countBookInLibrary = botLogic.getReader().getCountLinesInFile("src\\main\\resources\\library.txt");
        if (userData.getFlChoose()) {
            userData.getState().setCurrentState(State.state.Read);
            var number = 0;
            try {
                number = Integer.parseInt(message.getText());
            } catch (NumberFormatException e) {
                userData.getState().setCurrentState(State.state.Library);
                return "Неправильно выбран номер книги";
            }
            if (number <= 0 || countBookInLibrary < number) {
                userData.getState().setCurrentState(State.state.Library);
                return "Неправильно выбран номер книги";
            } else {
                userData.setCurrentBook(Integer.parseInt(message.getText()));
                botLogic.setCurrentParagraphsList(userData);
                return "Приятного чтения";
            }
        }
        userData.setFlChoose(true);
        return "";
    }
}
