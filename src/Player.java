import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Player implements Runnable {
    public Socket socket;
    public PrintWriter writer;
    public BufferedReader reader;
    public Server master;
    private Thread runningThread;
    private boolean running;
    public String username = "Anonymous";

    public Player(Socket socket) {
        try {
            this.socket = socket;
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running = true;
            sendMessageToClient("Welcome to our quiz game!");
            runningThread = new Thread(this);
            runningThread.start();
        } catch (Exception e) {
            disconnectPlayer();
        }
    }

    public boolean setAsHostIfPossible() {
        if (master.currentHost != null || !master.connectedPlayers.get(0).equals(this)) {
            return false;
        }
        master.currentHost = this;
        sendMessageToServer(username + socket.getRemoteSocketAddress() + " is now the host.");
        sendMessageToOtherClients(username + " is now the host.");
        sendMessageToClient("You are the host now.");
        return true;
    }

    public void registerServer(Server master) {
        this.master = master;
    }

    public void run() {
        sendMessageToClient("For more information about commands, use 'HELP' command.");
        while (running) {
            try {
                String command = reader.readLine();
                if(master.currentGame != null && checkGameCommand(command)) {
                    Integer convertToId = getQuestionId(command);
                    String convertToAnswer = getQuestionAnswer(command);
                    master.currentGame.receiveInput(this, convertToId, convertToAnswer);
                }
                if (command.equalsIgnoreCase("NEW GAME") && master.currentGame == null) {
                    try {
                        master.currentGame = startNewGame();
                    } catch (IOException | ClassNotFoundException e) {
                        sendMessageToClient("Couldn't retrieve questions from database. Try again...");
                        try {
                            QuestionBank.serialize(QuestionBank.generateList());
                        } catch (IOException ignored) {
                        }
                    }

                } else if (startsWithIgnoreCase(command, "USERNAME") && master.currentGame == null) {
                    changeUsername(command);
                } else if (command.equalsIgnoreCase("HELP")) {
                    if (master.currentGame == null) {
                        displayMenuCommands();
                    } else {
                        displayMenuCommands();
                    }
                } else if (command.equalsIgnoreCase("QUIT")) {
                    disconnectPlayer();
                } else if (master.currentGame == null) {
                    sendMessageToClient("Invalid command. For more information use 'HELP' command.");
                }
            } catch (IOException | NullPointerException e) {
                disconnectPlayer();
            }
        }
    }

    private Game startNewGame() throws IOException, ClassNotFoundException {
        if (!master.currentHost.equals(this)) {
            sendMessageToClient("Forbidden operation. You are not a host.");
        } else if (master.connectedPlayers.size() < master.minClients) {
            sendMessageToClient("Not enough players. At least 2 players should be connected to the server.");
        } else {
            ArrayList<Question> questions = QuestionBank.deserialize();
            ArrayList<Question> randomQuestions = QuestionBank.generateRandomList(questions);
            sendMessageToServer("A new game has started.");
            sendMessageToAllClients("Starting the game...");
            return new Game(master.connectedPlayers, this, randomQuestions);
        }
        return null;
    }

    private boolean checkGameCommand(String answer) {
        Integer id = getQuestionId(answer);
        String response = getQuestionAnswer(answer);
        return id != null && response != null;
    }

    private String getQuestionAnswer(String answer) {
        for (int i = 0; i < answer.length(); i++) {
            String currentChar = Character.toString(answer.charAt(i));
            if (currentChar.isBlank()) {
                return answer.substring(i + 1);
            }
        }
        return null;
    }

    private Integer getQuestionId(String answer) {
//        String stringId = "";
//        for (int i = 0; i < answer.length(); i++) {
//            String currentChar = Character.toString(answer.charAt(i));
//            if (!currentChar.isBlank()) {
//                stringId += currentChar;
//            } else {
//                break;
//            }
//        }
        int id;
        int blankPos = answer.indexOf(" ");
        String stringId = answer.substring(0,blankPos);

        try {
            id = Integer.parseInt(stringId);
        } catch (NumberFormatException e) {
            return null;
        }
        return id;
    }

    private void changeUsername(String command) {
        String previousUsername = username;
        username = command.substring("USERNAME".length() + 1);
        sendMessageToClient("Hello " + username + "!");
        sendMessageToServer(String.format("%s's%s new username is %s!", previousUsername, socket.getRemoteSocketAddress(), username));
        sendMessageToOtherClients(previousUsername + "'s new username is " + username + "!");
    }

    private void displayMenuCommands() {
        sendMessageToClient("""
                NEW GAME - start a new game, you can do it only as host.
                USERNAME <username> - enter your nickname which you will use in your game.
                HELP - lists all commands for use by the player.
                QUIT - quit the game.""");
    }

    private void disconnectPlayer() {
        master.connectedPlayersCount.decrementAndGet();
        master.connectedPlayers.remove(this);
        sendMessageToServer(username + socket.getRemoteSocketAddress() + " has left the server.");
        sendMessageToOtherClients(username + " has left the server.");
        if (master.currentHost.equals(this)) {
            master.currentHost = null;
            for (Player player : master.connectedPlayers) {
                if (player.setAsHostIfPossible()) {
                    break;
                }
            }
        }
        sendMessageToClient("Disconnecting...");
        sendMessageToClient("QUIT");
        disconnect();
    }

    private void disconnect() {
        running = false;
        if (runningThread != null)
            runningThread.interrupt();
        runningThread = null;
        try {
            reader.close();
        } catch (Exception ignored) {
        }
        reader = null;
        try {
            writer.close();
        } catch (Exception ignored) {
        }
        writer = null;
        try {
            socket.close();
        } catch (Exception ignored) {
        }
        socket = null;
    }

    private void sendMessageToAllClients(String message) {
        if (running) {
            for (Player player : master.connectedPlayers) {
                player.sendMessageToClient(message);
            }
        }
    }

    public void sendMessageToOtherClients(String message) {
        if (running) {
            for (Player player : master.connectedPlayers) {
                if (!player.equals(this)) {
                    player.sendMessageToClient(message);
                }
            }
        }
    }

    public void sendMessageToClient(String message) {
        if (running) {
            writer.println(message);
        }
    }

    private void sendMessageToServer(String message) {
        if (running) {
            System.out.println(message);
        }
    }

    private static boolean startsWithIgnoreCase(String message, String prefix) {
        return message.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}