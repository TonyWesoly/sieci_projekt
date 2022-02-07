import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Client implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Scanner scanner;
    private Thread runningThread;
    private boolean running;
    private static final int PORT = 8081;

    public Client() {
        try {
            socket = new Socket("127.0.0.1", PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);
            running = true;
            runningThread = new Thread(this);
            runningThread.start();
        } catch (Exception e) {
            sendMessageToClient("Couldn't connect to server. Disconnecting...");
            disconnect();
            System.exit(-1);
        }
    }

    public void run() {
        read.start();
        write.start();
    }

    public static void main(String[] args) {
        new Client();
    }

    private Thread write = new Thread() {
        public void run() {
            while (running) {
                sendMessageToServer(scanner.nextLine());
            }
        }
    };

    private Thread read = new Thread() {
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null && running) {
                    if (message.equalsIgnoreCase("QUIT")) {
                        disconnect();
                        System.exit(0);
                    } else {
                        sendMessageToClient(message);
                    }
                }
            } catch (Exception e) {
                sendMessageToClient("Couldn't receive a message from the server. Disconnecting...");
                disconnect();
                System.exit(-1);
            }
        }
    };

    private void disconnect() {
        running = false;
        if (write != null)
            write.interrupt();
        write = null;
        if (read != null)
            read.interrupt();
        read = null;
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

    private void sendMessageToClient(String message) {
        if (running) {
            System.out.println(message);
        }
    }

    private void sendMessageToServer(String message) {
        if (running) {
            writer.println(message);
        }
    }
}