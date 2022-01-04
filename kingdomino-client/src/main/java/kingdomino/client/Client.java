package kingdomino.client;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Client {

    private String ip;
    private int port;

    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    private List<String> order = new ArrayList<>();
    private int moves = 0;
    private int i = 3;

    public Client (String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean connect() {
        try {
            this.socket = new Socket(ip, port);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void login(String name) throws IOException {
        serverOut.write(("LOGIN " + name + '\n').getBytes());
        serverOut.flush();
    }

    public void chooseDominoFromStart(String message) throws IOException {
        String[] tokens = StringUtils.split(message);
        List<String> availableDominos = new ArrayList<>();
        for (int i = 6; i < tokens.length; i++) {
            availableDominos.add(tokens[i]);
        }
        Random rand = new Random();
        String domino = availableDominos.get(rand.nextInt(availableDominos.size()));
        System.out.println(domino);
        serverOut.write(("CHOOSE " + domino + '\n').getBytes());
        serverOut.flush();
    }

    public void chooseDomino(String message) throws IOException{
        String[] tokens = StringUtils.split(message);
        List<String> availableDominos = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            availableDominos.add(tokens[i]);
        }
        Random rand = new Random();
        String domino = availableDominos.get(rand.nextInt(availableDominos.size()));
        System.out.println(domino);
        serverOut.write(("CHOOSE " + domino + '\n').getBytes());
        serverOut.flush();
    }

    public void makeMove() throws IOException {
        List<Integer> list = Arrays.asList(0, 90, 180, 270);

        int x, y, orientation;

        Random random = new Random();

        if (moves == 0) {
            x = 0;
            y = 1;
            orientation = 90;
        } else {
            x = 0;
            y = i;
            orientation = 90;
            this.i += 2;
        }

        serverOut.write(("MOVE " + x + " " + y + " " + orientation +  '\n').getBytes());
        serverOut.flush();
        this.moves++;
    }

    public void sendInvalidMessage() throws IOException {
        serverOut.write(("12345" + '\n').getBytes());
        serverOut.flush();
    }

    public String getMessagesFromServer() throws IOException {
        //if (bufferedIn.ready()) {
        String fromServer = bufferedIn.readLine();
        //System.out.println(fromServer);
        return fromServer;
        //}
    }

    public void endConnection() throws IOException {
        socket.close();
        //serverOut.write(("." + '\n').getBytes());
        //serverOut.flush();
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getBufferedIn() {
        return bufferedIn;
    }
}
