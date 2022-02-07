import java.io.*;
import java.util.*;

public class Game implements Runnable {
    private Thread runningThread;
    private boolean running;
    private final HashMap<Player, PlayerStats> playerList = new HashMap<>();
    private final Player host;
    private final ArrayList<Question> questions;
    private Question currentQuestion;
    private final int intervalInSeconds = 5;
    private final int rewardPoints = 1;
    private final int penaltyPoints = -2;

    public Game(ArrayList<Player> playerList, Player host, ArrayList<Question> questions) {
        for (Player player : playerList) {
            this.playerList.put(player, new PlayerStats());
        }
        this.host = host;
        this.questions = questions;
        running = true;
        runningThread = new Thread(this);
        runningThread.start();
    }

    public void run() {
        while (running) {
            for (int i = 0; i < questions.size(); i++) {
                currentQuestion = questions.get(i);
                for (Player player : playerList.keySet()) {
                    player.sendMessageToClient(String.format("Question %d/%d", i + 1, questions.size()));
                    player.sendMessageToClient(questions.get(i).toString());
                }
                try {
                    Thread.sleep(1000 * intervalInSeconds);
                } catch (InterruptedException ignored) {
                }
                for (PlayerStats player : playerList.values()) {
                    player.hasAnsweredQuestion = false;
                }
            }
            for(Player player : playerList.keySet()) {
//                player.sendMessageToClient("Tu bedą statystyki");
                displayStatistics(player);
            }
            host.master.currentGame = null;
            stop();
        }
    }

    public void receiveInput(Player client, Integer id, String answer) {
        PlayerStats stats = playerList.get(client);
        if(stats.wrongAnswersInRow >= 3){
            client.sendMessageToClient("The player gave the wrong answer three times in a row. " +
                    "This question has been blocked.");
            stats.wrongAnswersInRow = 0;
        }
        else if(!playerList.get(client).hasAnsweredQuestion) {
            try {
                CheckAnswer(client, id, answer);
            } catch (IOException ignored) {
            }
        }
    }


    private void displayStatistics(Player player){
        for (Player playerToDisplay : playerList.keySet()){
            PlayerStats statsToDisplay = playerList.get(playerToDisplay);
            player.sendMessageToClient("Player \""+playerToDisplay.username+
                    "\"score: "+statsToDisplay.score);
        }
    }

    private void CheckAnswer(Player player, Integer id, String answer) throws IOException {
        PlayerStats stats = playerList.get(player);
        if (!currentQuestion.isGivenIdCorrect(id)) {
            player.sendMessageToClient("Invalid question ID. Try sending your answer again...");
            return;
        }
        boolean validAnswer = answer.equalsIgnoreCase("YES") || answer.equalsIgnoreCase("NO");
        if (!validAnswer) {
            player.sendMessageToClient("Invalid answer. Try sending your answer again...");
            return;
        }
        PlayerStats playerStats = playerList.get(player);
        if (currentQuestion.isAnsweredCorrectly) {
            player.sendMessageToClient("Someone has already answered this question correctly.");
            return;
        } else if (currentQuestion.isGivenAnswerCorrect(answer)) {
            playerStats.hasAnsweredQuestion = true;
            currentQuestion.isAnsweredCorrectly = true;
            if (runningThread.getState() == Thread.State.TIMED_WAITING) {
                runningThread.interrupt();
            }
            playerStats.score += rewardPoints;
            playerStats.wrongAnswersInRow = 0;
            player.sendMessageToClient("Your answer is correct!");
            player.sendMessageToOtherClients(player.username + "'s answer was correct!");
        } else {
            playerStats.hasAnsweredQuestion = true;
            playerStats.score += penaltyPoints;
            playerStats.wrongAnswersInRow++;
            player.sendMessageToClient("Your answer is incorrect!");
        }
    }

    private void stop() {
        running = false;
        if (runningThread != null)
            runningThread.interrupt();
        runningThread = null;
    }
}

class PlayerStats {
    public int score = 0;
    public int wrongAnswersInRow = 0;
    public boolean hasAnsweredQuestion = false;
}
