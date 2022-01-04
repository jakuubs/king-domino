package kingdomino.game;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class Player {

    private int id;
    private String login;
    private int chosenDomino = 0;
    private int moves = 0;
    private String lastMove = null;
    private List<Point> board = new ArrayList<>();
    private List<Point> occupiedPoints = new ArrayList<>();
    private Map<String, List<Point>> kingdoms = new HashMap<>();
    private int points = 0;

    public Player(int id, String login) {
        this.id = id;
        this.login = login;

        this.kingdoms.put("s", new ArrayList<>());
        this.kingdoms.put("f", new ArrayList<>());
        this.kingdoms.put("w", new ArrayList<>());
        this.kingdoms.put("g", new ArrayList<>());
        this.kingdoms.put("b", new ArrayList<>());
        this.kingdoms.put("m", new ArrayList<>());
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public int getChosenDomino() {
        return this.chosenDomino;
    }

    public void setChosenDomino(int chosenDomino) {
        this.chosenDomino = chosenDomino;
    }

    public int getMoves() {
        return this.moves;
    }

    public String getLastMove() {
        return this.lastMove;
    }

    public void setLastMove(String lastMove) {
        this.lastMove = lastMove;
    }

    public List<Point> getBoard() {
        return board;
    }

    public List<Point> getOccupiedPoints() {
        return occupiedPoints;
    }

    public int getPoints() {
        return points;
    }

    public void setPlayerMove(Point point1, Point point2) {
        String domino = KingDomino.getDominoValue(chosenDomino);
        //this.chosenDomino = 0;
        String[] dominoParts = StringUtils.split(domino);
        /*Point point1 = new Point(x, y);
        Point point2;

        switch (orientation) {
            case 0:
                point2 = new Point(x + 1, y);
                break;
            case 90:
                point2 = new Point(x, y - 1);
                break;
            case 180:
                point2 = new Point(x - 1, y);
            case 270:
                point2 = new Point(x, y + 1);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + orientation);
        }*/

        this.occupiedPoints.add(point1);
        this.occupiedPoints.add(point2);

        point1.setDominoPart(dominoParts[0]);
        point2.setDominoPart(dominoParts[1]);

        this.board.add(point1);
        this.board.add(point2);
        this.moves++;
    }

    public void addPointToKingdom(Point p) {
        String domino = p.getDominoPart();

        if (domino.startsWith("s"))
            this.kingdoms.get("s").add(p);
        else if (domino.startsWith("f"))
            this.kingdoms.get("f").add(p);
        else if (domino.startsWith("w"))
            this.kingdoms.get("w").add(p);
        else if (domino.startsWith("g"))
            this.kingdoms.get("g").add(p);
        else if (domino.startsWith("b"))
            this.kingdoms.get("b").add(p);
        else if (domino.startsWith("m"))
            this.kingdoms.get("m").add(p);
    }

    public void countPoints() {
        String[] kds = {"s", "f", "w", "g", "b", "m"};
        for (int i = 0; i < kds.length; i++) {
            List<Point> pointList = this.kingdoms.get(kds[i]);
            int kingdomPoints = 0;
            if (pointList.isEmpty())
                kingdomPoints = 0;
            else {
                List<List<Point>> adjacentPointsLists = new ArrayList<>();
                for (Point p1 : pointList) {
                    if (!adjacentPoint(p1, pointList)) {
                        if (p1.getDominoPart().length() > 1)
                            kingdomPoints += (1 + parseInt(p1.getDominoPart().substring(1)));
                        else
                            kingdomPoints++;
                        continue;
                    }
                    for (Point p2 : pointList) {
                        if (!p1.equals(p2)) {
                            if ((p1.getX() == p2.getX() + 1 && p1.getY() == p2.getY())
                                    || (p1.getX() == p2.getX() - 1 && p1.getY() == p2.getY())
                                    || (p1.getX() == p2.getX() && p1.getY() == p2.getY() + 1)
                                    || (p1.getX() == p2.getX() && p1.getY() == p2.getY() - 1)) {
                                if (anyConatins(p1, adjacentPointsLists)) {
                                    for (List<Point> adjacentPoints : adjacentPointsLists) {
                                        if (adjacentPoints.contains(p1) && !adjacentPoints.contains(p2)) {
                                            adjacentPoints.add(p2);
                                        }
                                        /*if (containsP1notP2(p1, p2, adjacentPoints)) {
                                            adjacentPoints.add(p2);
                                        }*/
                                    }
                                } else {
                                    List<Point> pl = new ArrayList<>();
                                    pl.add(p1);
                                    pl.add(p2);
                                    adjacentPointsLists.add(pl);
                                }
                            }
                        }
                    }
                }

                for (List<Point> adjacentPoints : adjacentPointsLists) {
                    int listPoints = 0;
                    int listCrowns = 0;
                    for (Point p : adjacentPoints) {
                        if (p.getDominoPart().length() > 1) {
                            listPoints ++;
                            listCrowns += parseInt(p.getDominoPart().substring(1));
                        } else
                            listPoints ++;
                    }
                    kingdomPoints += (listPoints * (1 + listCrowns));
                }
            }
            this.points += kingdomPoints;
        }
    }

    public boolean adjacentPoint(Point p, List<Point> points) {
        boolean ok = false;

        for (Point point : points) {
            if (!p.equals(point)) {
                if ((p.getX() == point.getX() + 1 && p.getY() == point.getY())
                        || (p.getX() == point.getX() - 1 && p.getY() == point.getY())
                        || (p.getX() == point.getX() && p.getY() == point.getY() + 1)
                        || (p.getX() == point.getX() && p.getY() == point.getY() - 1)) {
                    ok = true;
                }
            }
        }

        return ok;
    }

    public boolean anyConatins(Point p, List<List<Point>> adjacentPointsLists) {
        boolean ok = false;

        for (List<Point> adjacentPoints : adjacentPointsLists) {
            if (adjacentPoints.contains(p)) {
                ok = true;
                break;
            }
            /*for (Point point : adjacentPoints) {
                if (p.getX() == point.getX() && p.getY() == point.getY()) {
                    ok = true;
                    break;
                }
            }*/
        }

        return ok;
    }

    public boolean containsP1notP2(Point p1, Point p2, List<Point> adjacentPoints) {
        boolean ok = false;

        for (Point point : adjacentPoints) {
            if ((p1.getX() == point.getX() && p1.getY() == point.getY())) {
                ok = true;
                break;
            }
        }

        for (Point point : adjacentPoints) {
            if (p2.getX() == point.getX() && p2.getY() == point.getY()) {
                ok = false;
            }
        }

        return ok;
    }
}
