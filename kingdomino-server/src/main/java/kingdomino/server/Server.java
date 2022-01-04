package kingdomino.server;

import kingdomino.game.Player;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends Thread {

    private final int port;
    private static AtomicInteger clientsConnected = new AtomicInteger(0);
    private static AtomicInteger playersConnected = new AtomicInteger(0);

    private static List<ClientHandler> handlers = Collections.synchronizedList(new ArrayList<>());
    private static List<Player> players = Collections.synchronizedList(new ArrayList<>());

    private static BufferedWriter bufferedWriter;

    {
        try {
            bufferedWriter = new BufferedWriter(new FileWriter("log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server(int port) {
        this.port = port;
    }

    public static void addPlayer(Player player) {
        players.add(player);
    }

    public static void setPlayer(int id, Player player) {
        players.set(id - 1, player);
    }

    public static void addHandler(ClientHandler clientHandler) {
        handlers.add(clientHandler);
    }

    public static List<ClientHandler> getHandlers() {
        return handlers;
    }

    public static synchronized List<Player> getPlayers() {
        return players;
    }

    public static synchronized AtomicInteger getClientsConnected() {
        return clientsConnected;
    }

    public static synchronized void incPlayers() {
        playersConnected.incrementAndGet();
    }

    public static synchronized AtomicInteger getPlayersConnected() {
        return playersConnected;
    }

    public static void addLog(String message) throws IOException {
        bufferedWriter.write(message);
    }

    public static void closeWriter() throws IOException {
        bufferedWriter.close();
    }

    //Run new server instance and wait for connections
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            bufferedWriter.write("");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientsConnected.incrementAndGet();
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clientHandler.start();

                if (clientsConnected.intValue() == 4) {
                    GameStarter gameStarter = new GameStarter();
                    gameStarter.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                bufferedWriter.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
