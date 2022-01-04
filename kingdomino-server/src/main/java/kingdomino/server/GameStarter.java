package kingdomino.server;

public class GameStarter extends Thread {

    @Override
    public void run() {
        while(true) {
            if (Server.getPlayersConnected().intValue() == 4) {
                GameHandler gameHandler = new GameHandler();
                gameHandler.start();
                break;
            }
        }
    }
}
