package kingdomino;

import kingdomino.server.Server;

public class Main {

    public static void main(String[] args) {
        Server server = new Server(8765);
        server.start();
    }
}
