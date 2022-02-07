import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private boolean started;
    private boolean running;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private static final int PORT = 8081;
    public ArrayList<Player> connectedPlayers = new ArrayList<>();
    public AtomicInteger connectedPlayersCount = new AtomicInteger();
    public Game currentGame;
    public Player currentHost;
    public final int minClients = 2;
    public final int maxClients = 4;

    public Server() {
        started = false;
        serverSocket = null;
    }

    public void run() {
        while (running) {
            if (connectedPlayersCount.get() < maxClients && currentGame == null) {
                try {
                    Socket client = serverSocket.accept();
                    connectedPlayersCount.incrementAndGet();
                    sendMessageToServer(client.getRemoteSocketAddress() + " has connected.");
                    Player player = new Player(client);
                    player.registerServer(this);
                    connectedPlayers.add(player);
                    player.setAsHostIfPossible();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private void start() {
        if (!started) {
            started = true;
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                serverThread = new Thread(this);
                serverThread.start();
                System.out.println("Waiting for players on port " + serverSocket.getLocalPort() + "...");
            } catch (Exception e) {
                sendMessageToServer("Couldn't create the server. Shutting down...");
                stop();
                System.exit(-1);
            }
        }
    }

    private void stop() {
        running = false;
        started = false;
        if (serverThread != null)
            serverThread.interrupt();
        serverThread = null;
    }

    private void sendMessageToServer(String message) {
        if (running) {
            System.out.println(message);
        }
    }
}

