import java.util.ArrayList;
import java.util.HashMap;

public class UserDates {
    private State state = new State();
    private HashMap<String, BotPrimitive.MyFunc> currentCommands;
    private Boolean flChoose = false;
    private Boolean flEcho = false;
    private int currentBook = 0;
    private ArrayList<String> currentParagraphsList = new ArrayList<>();
    private int currentPosition = 0;
    private Quiz currentQuiz;

    public State getState() {
        return state;
    }

    public HashMap<String, BotPrimitive.MyFunc> getCurrentCommands() {
        return currentCommands;
    }

    public void setCurrentCommands(HashMap<String, BotPrimitive.MyFunc> currentCommands) {
        this.currentCommands = currentCommands;
    }

    public Boolean getFlChoose() {
        return flChoose;
    }

    public Boolean getFlEcho() {
        return flEcho;
    }

    public void setFlChoose(Boolean flChoose) {
        this.flChoose = flChoose;
    }

    public void setFlEcho(Boolean flEcho) {
        this.flEcho = flEcho;
    }

    public ArrayList<String> getCurrentParagraphsList() {
        return currentParagraphsList;
    }

    public int getCurrentBook() {
        return currentBook;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public Quiz getCurrentQuiz(){
        return currentQuiz;
    }

    public void setCurrentQuiz(Quiz quiz){
        this.currentQuiz = quiz;
    }

    public void setCurrentBook(int currentBook) {
        this.currentBook = currentBook;
    }

    public void setCurrentParagraphsList(ArrayList<String> currentParagraphsList) {
        this.currentParagraphsList = currentParagraphsList;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
