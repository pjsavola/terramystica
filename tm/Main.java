package tm;

import tm.action.*;
import tm.faction.Alchemists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
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

        MapData.init();

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

        if (false) {
            final JFrame frame = new JFrame();
            frame.setTitle("JMystica");
            final ImageIcon icon = new ImageIcon("tm.png");
            final Image image = icon.getImage();
            final JPanel imagePanel = new JPanel() {
                @Override
                public void paintComponent(Graphics g) {
                    g.drawImage(image, 0, 0, null);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(icon.getIconWidth(), icon.getIconHeight());
                }
            };
            final JPanel buttonPanel = new JPanel();
            final JButton startButton = new JButton("Start");
            final JButton loadButton = new JButton("Load");
            final JButton importButton = new JButton("Import");
            final JButton quitButton = new JButton("Quit");
            buttonPanel.add(startButton);
            buttonPanel.add(loadButton);
            buttonPanel.add(importButton);
            buttonPanel.add(quitButton);
            importButton.addActionListener(l -> {
                
            });
            quitButton.addActionListener(l -> frame.setVisible(false));
            buttonPanel.setBackground(Color.BLACK);
            final JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(imagePanel);
            mainPanel.add(buttonPanel);
            frame.setContentPane(mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
            return;
        }

        final GameData test = new GameData(1, new Random().nextInt());
        //final GameData test = new GameData("tests/Petri01");

        final JFrame frame = new JFrame();
        final Game game = new Game(frame, test);

        final int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        final int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        final JScrollPane jsp = new JScrollPane(game, v, h);

        frame.setContentPane(jsp);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        game.refresh();
        frame.setVisible(true);
    }

    public static boolean test(String file, int[] vpTargets) {
        try {
            final GameData test = new GameData(file);
            final JFrame frame = new JFrame();
            final Game game = new Game(frame, test);
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