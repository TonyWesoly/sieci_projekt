import java.io.*;

public class Question implements Serializable {
    private final int id;
    private final String question;
    private final String correctAnswer;
    public boolean isAnsweredCorrectly;

    public Question(int id, String question, String correctAnswer) {
        this.id = id;
        this.question = question;
        this.correctAnswer = correctAnswer;
        isAnsweredCorrectly = false;
    }

    public boolean isGivenIdCorrect(int id) {
        return this.id == id;
    }

    public boolean isGivenAnswerCorrect(String answer) {
        return answer.equalsIgnoreCase(correctAnswer);
    }

    @Override
    public String toString() {
        return String.format("Question ID: %d\n%s", id, question);
    }
}


