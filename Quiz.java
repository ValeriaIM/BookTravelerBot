public class Quiz {
    private String[] questions;
    private String[] answers;
    private String currentAnswer = "0";
    private int currentQuestion = 0;
    private int correctAnswers = 0;

    public Quiz(String[] questions, String[] answers) {
        this.questions = questions;
        this.answers = answers;
    }

    public String[] getQuestions() {
        return questions;
    }

    public String[] getAnswers() {
        return answers;
    }
    
    public String getCurrentAnswer() {
        return currentAnswer;
    }
    
    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    } //getters

    public void setCurrentAnswer(String currentAnswer) {
        this.currentAnswer = currentAnswer;
    }
    
    public void nextQuestion() {
        currentQuestion++;
    }

    public void addCorrectAnswers() {
        correctAnswers++;
    }
}
