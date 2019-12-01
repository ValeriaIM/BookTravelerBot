import java.util.ArrayList;
import java.util.HashMap;

public class Quiz {
    private ArrayList<String> questions;
    private ArrayList<String> answers;

    public Quiz(ArrayList<String> questions, ArrayList<String> answers){
        this.questions = questions;
        this.answers = answers;
    }

    public ArrayList<String> getQuestions(){
        return questions;
    }
    public ArrayList<String> getAnswers(){
        return answers;
    }
}
