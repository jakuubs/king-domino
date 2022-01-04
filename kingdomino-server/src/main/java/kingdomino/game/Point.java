package kingdomino.game;

import java.util.Objects;

public class Point {
    private int x;
    private int y;
    private String dominoPart;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(int x, int y, String dominoPart) {
        this.x = x;
        this.y = y;
        this.dominoPart = dominoPart;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getDominoPart() {
        return dominoPart;
    }

    public void setDominoPart(String dominoPart) {
        this.dominoPart = dominoPart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return getX() == point.getX() &&
                getY() == point.getY() &&
                Objects.equals(getDominoPart(), point.getDominoPart());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getDominoPart());
    }
}