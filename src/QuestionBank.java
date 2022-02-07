import java.io.*;
import java.util.*;

public final class QuestionBank {
    private final static String fileName = "Questions.txt";
    private final static int databaseQuestions = 20;
    private final static int quizQuestions = 10;

    public static ArrayList<Question> generateRandomList(ArrayList<Question> allQuestions) {
        ArrayList<Question> randomQuestions = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < quizQuestions; i++) {
            int randomIndex = random.nextInt(allQuestions.size());
            randomQuestions.add(allQuestions.get(randomIndex));
            allQuestions.remove(randomIndex);
        }
        return randomQuestions;
    }

    public static ArrayList<Question> generateList() {
        ArrayList<Question> questions = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < databaseQuestions; i++) {
            String randomAnswer;
            if(random.nextBoolean()) {
                randomAnswer = "YES";
            } else {
                randomAnswer = "NO";
            }
            Question question = new Question(i, "The content of the question.", randomAnswer);
            questions.add(question);
        }
        return questions;
    }

    public static void serialize(Object questions) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
        objectOutputStream.writeObject(questions);
        objectOutputStream.close();
    }

    public static ArrayList<Question> deserialize() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(fileName);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        ArrayList<Question> questions = (ArrayList<Question>) objectInputStream.readObject();
        objectInputStream.close();
        return questions;
    }
}
