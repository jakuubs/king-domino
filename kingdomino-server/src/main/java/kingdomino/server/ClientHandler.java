package kingdomino.server;

import kingdomino.game.Player;
import kingdomino.game.Point;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class ClientHandler extends Thread {

    private final Socket clientSocket;
    private Server server;
    private Player player = null;
    private OutputStream output;

    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public Player getPlayer() {
        return player;
    }

    //Run a new handler when player connects
    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream input = clientSocket.getInputStream();
        this.output = clientSocket.getOutputStream();

        //Send message after connection
        output.write(("CONNECT" + '\n').getBytes());
        Server.addLog("CONNECT" + '\n');
        System.out.println("CONNECT");
        output.flush();
        Thread.sleep(100);

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;

        //Loop for receiving messages
        while ((line = reader.readLine()) != null) {
            //Thread.sleep(100);
            System.out.println(line);
            Server.addLog(line + '\n');
            String[] tokens = StringUtils.split(line);

            //Check if message is not empty
            if (tokens != null && tokens.length > 0) {
                //Break the loop
                if (line.equals(".")) {
                    break;
                }
                //Login a new player
                else if (tokens[0].equals("LOGIN")) {
                    handlePlayerLogin(output, tokens);
                }
                //Choose a domino if logged in
                else if (this.player != null) {
                    if (tokens[0].equals("CHOOSE") && this.player.getChosenDomino() == 0) {
                        handlePlayerChoice(output, tokens);
                    } else if (tokens[0].equals("MOVE") && this.player.getLastMove() == null) {
                        handlePlayerMove(output, tokens);
                    } else {
                        output.write("ERROR\n".getBytes());
                        Server.addLog("ERROR" + '\n');
                        System.out.println("ERROR");
                        output.flush();
                    }
                }
                //Error if a player sends incorrect message
                else {
                    output.write("ERROR\n".getBytes());
                    Server.addLog("ERROR" + '\n');
                    System.out.println("ERROR");
                    output.flush();
                }
            }
        }
        clientSocket.close();
    }

    //Send message to client
    public void send(String message) throws IOException {
        output.write((message + '\n').getBytes());
        output.flush();
    }

    //Login a new player
    private synchronized void handlePlayerLogin(OutputStream output, String[] tokens) throws IOException, InterruptedException {
        if (tokens.length == 2) {
            Server.incPlayers();
            player = new Player(Server.getPlayersConnected().intValue(), tokens[1]);
            Server.addPlayer(player);
            Server.addHandler(this);
            output.write("OK\n".getBytes());
            Server.addLog("OK" + '\n');
            System.out.println("OK");
            output.flush();
        }
    }

    //Choose a domino
    private synchronized void handlePlayerChoice(OutputStream output, String[] tokens) throws IOException {
        if (tokens.length == 2) {
            boolean ok = false;
            boolean ok1;

            if (GameHandler.getGame().getRandomDominoValue(parseInt(tokens[1])) == null) {
                /*output.write("ERROR\n".getBytes());
                output.flush();*/
                ok1 = false;
            } else
                ok1 = true;

            //Check if other player had picked the domino before
            for (ClientHandler handler : Server.getHandlers()) {
                if (!this.equals(handler)) {
                    if (handler.getPlayer().getChosenDomino() == parseInt(tokens[1])) {
                        /*output.write("ERROR\n".getBytes());
                        output.flush();*/
                        ok = false;
                        break;
                    } else {
                        ok = true;
                    }
                }
            }

            //Pick the domino for the player when it is available
            if (ok && ok1) {
                output.write("OK\n".getBytes());
                Server.addLog("OK" + '\n');
                System.out.println("OK");
                output.flush();
                this.player.setChosenDomino(parseInt(tokens[1]));
                Server.setPlayer(this.player.getId(), this.player);
                /*output.write("OK\n".getBytes());
                output.flush();*/
            } else {
                output.write("ERROR\n".getBytes());
                Server.addLog("ERROR" + '\n');
                System.out.println("ERROR");
                output.flush();
            }
        }
    }

    private synchronized void handlePlayerMove(OutputStream output, String[] tokens) throws IOException {
        if (tokens.length == 4) {
            int x = parseInt(tokens[1]);
            int y = parseInt(tokens[2]);
            int orientation = parseInt(tokens[3]);

            Point point1 = new Point(x, y);
            Point point2;

            switch (orientation) {
                case 0:
                    point2 = new Point(x + 1, y);
                    break;
                case 90:
                    point2 = new Point(x, y + 1);
                    break;
                case 180:
                    point2 = new Point(x - 1, y);
                    break;
                case 270:
                    point2 = new Point(x, y - 1);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + orientation);
            }

            //if (this.getPlayer().getMoves() != 1 && )
            if (checkFirstMove(x, y, orientation) && checkIfTouchesOtherDomino(point1, point2) && checkIfAlreadyOccupied(point1, point2)) {
                //player.nextMove();
                output.write("OK\n".getBytes());
                Server.addLog("OK" + '\n');
                System.out.println("OK");
                output.flush();
                player.setLastMove(x + " " + y + " " + orientation);
                player.setPlayerMove(point1, point2);
                Server.setPlayer(player.getId(), player);
                //output.write("OK\n".getBytes());
                //output.flush();
                //player.setLastMove(null);
                //Server.setPlayer(player.getId(), player);
            } else {
                output.write("ERROR\n".getBytes());
                Server.addLog("ERROR" + '\n');
                System.out.println("ERROR");
                output.flush();
            }
        }
    }

    public boolean checkFirstMove(int x, int y, int orientation) {
        boolean ok = true;

        if (this.getPlayer().getMoves() == 0) {
            if ((x == 1 && y == 0 && orientation != 180) || (x == 0 && y == -1 && orientation != 90)
                    || (x == -1 && y == 0 && orientation != 0) || (x == 0 && y == 1 && orientation != 270))
                ok = true;
            else if ((x == 2 && y == 0 && orientation == 180) || (x == 0 && y == -2 && orientation == 90)
                    || (x == -2 && y == 0 && orientation == 0) || (x == 0 && y == 2 && orientation == 270))
                ok = true;
            else
                ok = false;
        }

        return ok;
    }

    public boolean checkIfTouchesOtherDomino(Point p1, Point p2) {
        boolean ok = true;

        if (this.getPlayer().getMoves() != 0) {
            for (int i = 0; i < this.getPlayer().getOccupiedPoints().size(); i++) {
                if ((p1.getX() + 1 == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p1.getY() == this.getPlayer().getOccupiedPoints().get(i).getY())
                        || (p1.getX() == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p1.getY() - 1 == this.getPlayer().getOccupiedPoints().get(i).getY())
                        || (p1.getX() - 1 == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p1.getY() == this.getPlayer().getOccupiedPoints().get(i).getY())
                        || (p1.getX() == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p1.getY() + 1 == this.getPlayer().getOccupiedPoints().get(i).getY()))
                    return true;
                else if ((p2.getX() + 1 == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p2.getY() == this.getPlayer().getOccupiedPoints().get(i).getY())
                        || (p2.getX() == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p2.getY() - 1 == this.getPlayer().getOccupiedPoints().get(i).getY())
                        || (p2.getX() - 1 == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p2.getY() == this.getPlayer().getOccupiedPoints().get(i).getY())
                        || (p2.getX() == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p2.getY() + 1 == this.getPlayer().getOccupiedPoints().get(i).getY()))
                    return true;
                else
                    ok = false;
            }
        }

        return ok;
    }

    public boolean checkIfAlreadyOccupied(Point p1, Point p2) {
        boolean ok = true;

        if (this.getPlayer().getMoves() != 0) {
            for (int i = 0; i < this.getPlayer().getOccupiedPoints().size(); i++) {
                if ((p1.getX() == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p1.getY() == this.getPlayer().getOccupiedPoints().get(i).getY())
                        || (p2.getX() == this.getPlayer().getOccupiedPoints().get(i).getX()
                        && p2.getY() == this.getPlayer().getOccupiedPoints().get(i).getY()))
                    return false;
                else
                    ok = true;
            }
        }

        return ok;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientHandler that = (ClientHandler) o;
        return Objects.equals(clientSocket, that.clientSocket) &&
                Objects.equals(server, that.server) &&
                Objects.equals(getPlayer(), that.getPlayer()) &&
                Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientSocket, server, getPlayer(), output);
    }
}
