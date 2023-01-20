package tm;

import tm.action.ConvertAction;
import tm.action.PassAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class Main {
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
        final String[] mapData = {
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
        final int seed = new Random().nextInt();
        final int playerCount = 1;
        final Game game = new Game(playerCount, mapData, seed);

        final Menu actionMenu = new Menu("Actions");
        final MenuItem passAction = new MenuItem("Pass");
        passAction.addActionListener(l -> game.resolveAction(new PassAction()));
        actionMenu.add(passAction);

        final MenuBar menuBar = new MenuBar();
        menuBar.add(actionMenu);

        final int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        final int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        final JScrollPane jsp = new JScrollPane(game, v, h);

        final JFrame frame = new JFrame();
        frame.setTitle("Terra Mystica");
        frame.setContentPane(jsp);
        frame.setMenuBar(menuBar);
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
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER -> game.confirmTurn();
                    case KeyEvent.VK_C -> game.resolveAction(new ConvertAction(Resources.c1));
                    case KeyEvent.VK_ESCAPE -> game.rewind();
                }
            }
        });
        frame.setVisible(true);
    }
}