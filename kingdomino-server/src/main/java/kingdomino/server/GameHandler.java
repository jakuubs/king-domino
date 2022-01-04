package kingdomino.server;

import kingdomino.game.KingDomino;
import kingdomino.game.Point;

import java.util.*;
import java.util.stream.Collectors;

public class GameHandler extends Thread {

    private static KingDomino game;
    private List<Integer> prevDominos = new ArrayList<>();

    @Override
    public void run() {
        try {
            boolean loggedIn = false;

            while (!loggedIn) {

                //Start game after every player logged in
                if (Server.getPlayers().size() == 4) {
                    loggedIn = true;
                    game = new KingDomino();

                    List<Integer> playerIds = new ArrayList<>();
                    Server.getPlayers().forEach(p -> playerIds.add(p.getId()));

                    //Shuffle the player order and get random dominos for the first round
                    Collections.shuffle(playerIds);
                    String order = playerIds.stream().map(Object::toString).
                            collect(Collectors.joining(" "));
                    String drawnDominos = game.drawRandomDominos(4);

                    //Send start message to all players
                    for (ClientHandler handler : Server.getHandlers()) {
                        handler.send("START " + handler.getPlayer().getId() +
                                " " + order + " " + drawnDominos);
                        Server.addLog("START " + handler.getPlayer().getId() +
                                " " + order + " " + drawnDominos + '\n');
                        System.out.println("START " + handler.getPlayer().getId() +
                                " " + order + " " + drawnDominos);
                    }

                    //Send choose message to all players
                    for (int id : playerIds) {
                        for (ClientHandler handler : Server.getHandlers()) {
                            if (handler.getPlayer().getId() == id) {
                                handler.send("YOUR CHOICE");
                                Server.addLog("YOUR CHOICE" + '\n');
                                System.out.println("YOUR CHOICE");
                                //Thread.sleep(100);
                                while (handler.getPlayer().getChosenDomino() == 0) {
                                    Thread.sleep(100);
                                    continue;
                                }
                                for (ClientHandler clientHandler : Server.getHandlers()) {
                                    if (!clientHandler.equals(handler)) {
                                        clientHandler.send("PLAYER CHOICE " + handler.getPlayer().getId() + " " + handler.getPlayer().getChosenDomino());
                                        Server.addLog("PLAYER CHOICE " + handler.getPlayer().getId() + " " + handler.getPlayer().getChosenDomino() + '\n');
                                        System.out.println("PLAYER CHOICE " + handler.getPlayer().getId() + " " + handler.getPlayer().getChosenDomino());
                                    }
                                }
                            }
                        }
                    }

                    for (int i = 0; i < 12; i++) {
                        for (int key : game.getRandomDominos().keySet()) {
                            prevDominos.add(key);
                        }

                        Collections.sort(prevDominos);
                        game.clearDominos();

                        drawnDominos = game.drawRandomDominos(4);

                        //Send next round dominos message to all players
                        for (ClientHandler handler : Server.getHandlers()) {
                            handler.send("ROUND " + drawnDominos);
                            Server.addLog("ROUND " + drawnDominos + '\n');
                            System.out.println("ROUND " + drawnDominos);
                        }

                        //Send move message to all players
                        for (int dominoNumber : prevDominos) {
                            for (ClientHandler handler : Server.getHandlers()) {
                                if (handler.getPlayer().getChosenDomino() == dominoNumber) {
                                    handler.send("YOUR MOVE");
                                    Server.addLog("YOUR MOVE" + '\n');
                                    //Thread.sleep(100);
                                    System.out.println("YOUR MOVE");
                                    while (handler.getPlayer().getLastMove() == null) {
                                        Thread.sleep(100);
                                        continue;
                                    }
                                    for (ClientHandler clientHandler : Server.getHandlers()) {
                                        if (!clientHandler.equals(handler)) {
                                            clientHandler.send("PLAYER MOVE " + handler.getPlayer().getId() + " " + handler.getPlayer().getLastMove());
                                            Server.addLog("PLAYER MOVE " + handler.getPlayer().getId() + " " + handler.getPlayer().getLastMove() + '\n');
                                            System.out.println("PLAYER MOVE " + handler.getPlayer().getId() + " " + handler.getPlayer().getLastMove());
                                        }
                                    }
                                    handler.getPlayer().setLastMove(null);
                                    handler.getPlayer().setChosenDomino(0);
                                    Server.setPlayer(handler.getPlayer().getId(), handler.getPlayer());

                                    if (drawnDominos != "") {
                                        handler.send("YOUR CHOICE");
                                        Server.addLog("YOUR CHOICE" + '\n');
                                        //Thread.sleep(100);
                                        System.out.println("YOUR CHOICE");
                                        while (handler.getPlayer().getChosenDomino() == 0) {
                                            Thread.sleep(100);
                                            continue;
                                        }
                                        for (ClientHandler clientHandler : Server.getHandlers()) {
                                            if (!clientHandler.equals(handler)) {
                                                clientHandler.send("PLAYER CHOICE " + handler.getPlayer().getId() + " " + handler.getPlayer().getChosenDomino());
                                                Server.addLog("PLAYER CHOICE " + handler.getPlayer().getId() + " " + handler.getPlayer().getChosenDomino() + '\n');
                                                System.out.println("PLAYER CHOICE " + handler.getPlayer().getId() + " " + handler.getPlayer().getChosenDomino());
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        prevDominos.clear();
                    }

                    String results = gameOver();

                    for (ClientHandler handler : Server.getHandlers()) {
                        handler.send(results);
                        Server.addLog(results + '\n');
                        System.out.println(results);
                    }

                    Server.closeWriter();
                    //System.exit(0);
                } //else
                //continue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KingDomino getGame() {
        return game;
    }

    public String gameOver() {
        for (ClientHandler handler : Server.getHandlers()) {
            System.out.println(handler.getPlayer().getLogin());
            for (Point p : handler.getPlayer().getBoard()) {
                handler.getPlayer().addPointToKingdom(p);
                System.out.println(p.getX() + " " + p.getY() + " " + p.getDominoPart());
            }
            Server.setPlayer(handler.getPlayer().getId(), handler.getPlayer());
        }

        List<Integer> playerPoints = new ArrayList<>();
        for (ClientHandler handler : Server.getHandlers()) {
            handler.getPlayer().countPoints();
            Server.setPlayer(handler.getPlayer().getId(), handler.getPlayer());
            playerPoints.add(handler.getPlayer().getPoints());
        }

        Collections.sort(playerPoints, Collections.reverseOrder());

        String gameOver = "GAME OVER RESULTS ";
        //List<String> gameOverResults = new ArrayList<>();

        List<Integer> usedPlayers = new ArrayList<>();

        for (int points : playerPoints) {
            for (ClientHandler handler : Server.getHandlers()) {
                if (handler.getPlayer().getPoints() == points
                        && !usedPlayers.contains(handler.getPlayer().getId())) {
                    //gameOverResults.add(handler.getPlayer().getLogin() + " " + handler.getPlayer().getPoints() + " ");
                    gameOver += handler.getPlayer().getLogin() + " " + handler.getPlayer().getPoints() + " ";
                    usedPlayers.add(handler.getPlayer().getId());
                }
            }
        }

        return gameOver.trim();
    }
}
