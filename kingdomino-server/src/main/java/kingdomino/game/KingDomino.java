package kingdomino.game;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class KingDomino {

    public KingDomino() {
        allDominos = new HashMap<>();
        allDominos.putAll(dominos);
    }

    private Map<Integer, String> dominos = new HashMap<>() {{
        put(1, "s s"); put(2, "s s"); put(3, "f f"); put(4, "f f"); put(5, "f f"); put(6, "f f");
        put(7, "w w"); put(8, "w w"); put(9, "w w"); put(10, "g g"); put(11, "g g"); put(12, "b b");
        put(13, "s f"); put(14, "s w"); put(15, "s g"); put(16, "s b"); put(17, "f w"); put(18, "f g");
        put(19, "s1 f"); put(20, "s1 w"); put(21, "s1 g"); put(22, "s1 b"); put(23, "s1 m"); put(24, "f1 s");
        put(25, "f1 s"); put(26, "f1 s"); put(27, "f1 s"); put(28, "f1 w"); put(29, "f1 g"); put(30, "w1 s");
        put(31, "w1 s"); put(32, "w1 f"); put(33, "w1 f"); put(34, "w1 f"); put(35, "w1 f"); put(36, "s g1");
        put(37, "w g1"); put(38, "s b1"); put(39, "g b1"); put(40, "m1 s"); put(41, "s g2"); put(42, "w g2");
        put(43, "s b2"); put(44, "g b2"); put(45, "m2 s"); put(46, "b m2"); put(47, "b m2"); put(48, "s m3");
    }};

    private static Map<Integer, String> allDominos;

    private Map<Integer, String> randomDominos = new HashMap<>();

    public Map<Integer, String> getDominos() {
        return dominos;
    }

    public Map<Integer, String> getRandomDominos() {
        return this.randomDominos;
    }

    public void clearDominos() {
        randomDominos.clear();
    }

    public static String getDominoValue(int key) {
        return allDominos.get(key);
    }

    public String getRandomDominoValue(int key) {
        return this.randomDominos.get(key);
    }

    public String drawRandomDominos(int howMany) {
        Random generator = new Random();
        int randomValue = 0;

        if (!dominos.isEmpty()) {
            for (int i = 0; i < howMany; i++) {
                Object[] values = dominos.keySet().toArray(new Integer[0]);
                randomValue = (int) values[generator.nextInt(values.length)];
                this.randomDominos.put(randomValue, dominos.get(randomValue));
                dominos.remove(randomValue);
            }
        }

        List<Integer> drawnDominos = new ArrayList<>(randomDominos.keySet());
        Collections.sort(drawnDominos);

        String availableDominos = StringUtils.join(drawnDominos, " ");

        return availableDominos;
    }
}
