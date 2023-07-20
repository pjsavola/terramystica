package tm;

import tm.action.*;
import tm.faction.Alchemists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static final int maxReplayActionCount = 2000;

    public static Hex.Type getType(String type) {
        return switch (type) {
            case "K" -> Hex.Type.BLACK;
            case "U" -> Hex.Type.BROWN;
            case "Y" -> Hex.Type.YELLOW;
            case "R" -> Hex.Type.RED;
            case "S" -> Hex.Type.GRAY;
            case "G" -> Hex.Type.GREEN;
            case "B" -> Hex.Type.BLUE;
            case "I" -> Hex.Type.WATER;
            default -> throw new RuntimeException("Unknown type: " + type);
        };
    }

    public static void main(String[] args) {

        final Map<String, int[]> tests = new HashMap<>();
        tests.put("tests/Petri01", new int[] {145, 120, 94, 142});
        //tests.put("tests/Petri02", new int[] {177, 162, 76}); -- Dodgy resource manipulation!
        tests.put("tests/Petri03", new int[] {113, 120, 176});
        tests.put("tests/Petri04", new int[] {151, 168, 109});
        tests.put("tests/Petri05", new int[] {150, 145, 126});
        tests.put("tests/Petri06", new int[] {152, 164, 166});
        tests.put("tests/Petri07", new int[] {127, 164, 119});
        tests.put("tests/Petri08", new int[] {161, 109, 120});
        tests.put("tests/Petri09", new int[] {126, 152, 145});
        tests.put("tests/Petri10", new int[] {110, 153, 146});
        //tests.put("tests/Petri11", new int[] {121, 110, 159}); -- Has illegal moves!
        tests.put("tests/Petri12", new int[] {103, 156, 111});
        tests.put("tests/Petri13", new int[] {138, 110, 138});
        tests.put("tests/Petri14", new int[] {169, 111, 119});
        tests.put("tests/Petri15", new int[] {162, 127, 111});
        tests.put("tests/Petri16", new int[] {116, 149, 98});
        tests.put("tests/Petri17", new int[] {82, 169, 104});
        tests.put("tests/Petri18", new int[] {120, 128, 116, 152});
        tests.put("tests/Petri19", new int[] {93, 126, 150, 140});
        tests.put("tests/Petri20", new int[] {131, 202, 104, 145});

        if (maxReplayActionCount >= 2000) {
            tests.forEach((file, vps) -> {
                if (!test(file, vps))
                    throw new RuntimeException("Test " + file + " failed!");
            });
        }


        final String[] arrowMapData = {
                "G,B,Y,U,G,Y,R,B,S,G,S,G,K",
                "R,K,B,I,I,I,I,I,I,Y,R,B",
                "S,U,I,I,S,R,Y,U,R,I,I,K,Y",
                "B,R,I,G,B,K,I,S,B,I,G,U",
                "G,S,G,I,U,I,I,I,G,I,I,I,I",
                "U,I,I,Y,K,R,I,K,Y,R,I,K",
                "R,K,Y,I,I,S,Y,B,U,I,I,U,B",
                "B,S,U,I,I,I,I,I,I,B,G,S",
                "K,R,G,S,K,S,G,K,R,Y,U,R,Y",
        };
        final String[] baseMapData = {
                "U,S,G,B,Y,R,U,K,R,G,B,R,K",
                "Y,I,I,U,K,I,I,Y,K,I,I,Y",
                "I,I,K,I,S,I,G,I,G,I,S,I,I",
                "G,B,Y,I,I,R,B,I,R,I,R,U",
                "K,U,R,B,K,U,S,Y,I,I,G,K,B",
                "S,G,I,I,Y,G,I,I,I,U,S,U",
                "I,I,I,S,I,R,I,G,I,Y,K,B,Y",
                "Y,B,U,I,I,I,B,K,I,S,U,S",
                "R,K,S,B,R,G,Y,U,S,I,B,G,R",
        };

        final GameData test = new GameData(1, new Random().nextInt());
        //final GameData test = new GameData("tests/Petri01");

        final JFrame frame = new JFrame();
        final Game game = new Game(frame, baseMapData, test);

        final int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        final int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        final JScrollPane jsp = new JScrollPane(game, v, h);

        frame.setTitle("Terra Mystica");
        frame.setContentPane(jsp);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (game.phase) {
                    case ACTIONS:
                    case CONFIRM_ACTION:
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_ESCAPE -> game.rewind();
                        }
                        break;
                    case LEECH:
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_ENTER -> game.resolveAction(new LeechAction(true));
                            case KeyEvent.VK_ESCAPE -> game.resolveAction(new LeechAction(false));
                        }
                        break;
                }
            }
        });
        game.refresh();
        frame.setVisible(true);
    }

    public static boolean test(String file, int[] vpTargets) {
        try {
            final String[] baseMapData = {
                    "U,S,G,B,Y,R,U,K,R,G,B,R,K",
                    "Y,I,I,U,K,I,I,Y,K,I,I,Y",
                    "I,I,K,I,S,I,G,I,G,I,S,I,I",
                    "G,B,Y,I,I,R,B,I,R,I,R,U",
                    "K,U,R,B,K,U,S,Y,I,I,G,K,B",
                    "S,G,I,I,Y,G,I,I,I,U,S,U",
                    "I,I,I,S,I,R,I,G,I,Y,K,B,Y",
                    "Y,B,U,I,I,I,B,K,I,S,U,S",
                    "R,K,S,B,R,G,Y,U,S,I,B,G,R",
            };
            final GameData test = new GameData(file);
            final JFrame frame = new JFrame();
            final Game game = new Game(frame, baseMapData, test);
            final int[] vps = game.getVictoryPoints();
            if (Arrays.equals(vpTargets, vps)) {
                return true;
            }
            System.err.println("Wrong vps: " + Arrays.stream(vps).mapToObj(String::valueOf).collect(Collectors.joining(",")));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}