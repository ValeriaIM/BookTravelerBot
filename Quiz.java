public class Quiz {
    private String[] questions;
    private String[] answers;
    private int currentQuestion = 0;
    private String currentAnswer = "0";
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

    public void nextQuestion() {
        currentQuestion++;
    }

    public void addCorrectAnswers() {
        correctAnswers++;
    }

    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public String getCurrentAnswer() {
        return currentAnswer;
    }

    public void setCurrentAnswer(String currentAnswer) {
        this.currentAnswer = currentAnswer;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }
}
