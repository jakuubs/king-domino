package kingdomino;

import kingdomino.client.Client;

import java.io.IOException;

public class GameRunner {

    public static void run(String ip, int port, String login) throws IOException {
        Client client = new Client(ip, port);
        String startMessage = null;
        String roundMessage = null;
        String prevMessage = null;
        if (!client.connect())
            System.out.println("Connection failed");
        else {
            System.out.println("Connection succesful");
            while(true) {
                String message = client.getMessagesFromServer();
                if (message.startsWith("GAME")) {
                    System.out.println(message);
                    client.endConnection();
                    break;
                } else if (message.startsWith("CONNECT")) {
                    System.out.println(message);
                    client.login(login);
                } else if (message.startsWith("START")) {
                    System.out.println(message);
                    startMessage = message;
                } else if (message.startsWith("ROUND")) {
                    startMessage = null;
                    System.out.println(message);
                    roundMessage = message;
                } else if (message.equals("YOUR CHOICE")) {
                    System.out.println(message);
                    prevMessage = message;
                    if (roundMessage != null)
                        client.chooseDomino(roundMessage);
                    else {
                        client.chooseDominoFromStart(startMessage);
                        //startMessage = null;
                    }
                    /*client.chooseDominoFromStart(startMessage);
                    startMessage = null;*/
                } else if (message.equals("YOUR MOVE")) {
                    System.out.println(message);
                    prevMessage = message;
                    client.makeMove();
                } else if (message.equals("ERROR")) {
                    System.out.println(message);
                    if (prevMessage.equals("YOUR CHOICE") && startMessage != null) {
                        client.chooseDominoFromStart(startMessage);
                        //startMessage = null;
                    } else if (prevMessage.equals("YOUR CHOICE") && startMessage == null) {
                        client.chooseDomino(roundMessage);
                    } else if (prevMessage.equals("YOUR MOVE"))
                        client.makeMove();
                } else
                    System.out.println(message);
            }
        }
        client.getSocket().close();
    }
}
