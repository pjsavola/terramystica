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
        tests.put("tests/Petri16", new int[] {116, 149, 98});
        tests.put("tests/Petri17", new int[] {82, 169, 104});
        tests.put("tests/Petri18", new int[] {120, 128, 116, 152});
        tests.put("tests/Petri19", new int[] {93, 126, 150, 140});
        tests.put("tests/Petri20", new int[] {131, 202, 104, 145});

        tests.forEach((file, vps) -> {
            if (!test(file, vps))
                throw new RuntimeException("Test " + file + " failed!");
        });

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
        final Menu actionMenu = new Menu("Actions");
        final GameData solo = new GameData(1, new Random().nextInt());
        final GameData test = new GameData("tests/Petri17");

        final JFrame frame = new JFrame();
        final Game game = new Game(frame, baseMapData, test, actionMenu);

        final MenuItem convertAction = new ActionMenuItem("Convert") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION;
            }
        };
        convertAction.addActionListener(l -> {
            if (game.phase != Game.Phase.ACTIONS && game.phase != Game.Phase.CONFIRM_ACTION) return;

            final boolean alchemists = game.getCurrentPlayer().getFaction() instanceof Alchemists;
            final JTextField priestsToWorkers = new JTextField();
            final JTextField workersToCoins = new JTextField();
            final JTextField powerToPriests = new JTextField();
            final JTextField powerToWorkers = new JTextField();
            final JTextField powerToCoins = new JTextField();
            final JTextField pointsToCoins = new JTextField();
            Object[] message = {
                    "P -> W:", priestsToWorkers,
                    "W -> C:", workersToCoins,
                    "5 PW -> P", powerToPriests,
                    "3 PW -> W", powerToWorkers,
                    "1 PW -> C", powerToCoins,
                    "1 VP -> C", pointsToCoins
            };
            message = Arrays.stream(message).limit(alchemists ? 12 : 10).toArray();

            int option = JOptionPane.showConfirmDialog(null, message, "Convert", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    final int p2w = priestsToWorkers.getText().isEmpty() ? 0 : Integer.parseInt(priestsToWorkers.getText());
                    final int w2c = workersToCoins.getText().isEmpty() ? 0 : Integer.parseInt(workersToCoins.getText());
                    final int vp2c = pointsToCoins.getText().isEmpty() ? 0 : Integer.parseInt(pointsToCoins.getText());
                    final int pw2p = powerToPriests.getText().isEmpty() ? 0 : Integer.parseInt(powerToPriests.getText());
                    final int pw2w = powerToWorkers.getText().isEmpty() ? 0 : Integer.parseInt(powerToWorkers.getText());
                    final int pw2c = powerToCoins.getText().isEmpty() ? 0 : Integer.parseInt(powerToCoins.getText());
                    Resources powerConversions = Resources.zero;
                    if (pw2p > 0) powerConversions = powerConversions.combine(Resources.fromPriests(pw2p));
                    if (pw2w > 0) powerConversions = powerConversions.combine(Resources.fromWorkers(pw2w));
                    if (pw2c > 0) powerConversions = powerConversions.combine(Resources.fromCoins(pw2c));
                    if (powerConversions != Resources.zero || p2w > 0 || w2c > 0 || vp2c > 0) {
                        game.resolveAction(new ConvertAction(powerConversions, p2w, w2c, vp2c, 0));
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid number: " + ex.getMessage());
                }
            }
        });
        actionMenu.add(convertAction);

        final MenuItem advanceShipAction = new ActionMenuItem("Advance ship") {
            @Override
            public boolean canExecute(Game game) {
                return game.getCurrentPlayer().canAdvanceShipping() && game.phase == Game.Phase.ACTIONS;
            }
        };
        advanceShipAction.addActionListener(l -> game.resolveAction(new AdvanceAction(false)));
        actionMenu.add(advanceShipAction);

        final MenuItem advanceDigAction = new ActionMenuItem("Advance dig") {
            @Override
            public boolean canExecute(Game game) {
                return game.getCurrentPlayer().canAdvanceDigging() && game.phase == Game.Phase.ACTIONS;
            }
        };
        advanceDigAction.addActionListener(l -> game.resolveAction(new AdvanceAction(true)));
        actionMenu.add(advanceDigAction);

        final MenuItem passAction = new ActionMenuItem("Final Pass") {
            @Override
            public boolean canExecute(Game game) {
                return game.getRound() == 6 && game.phase == Game.Phase.ACTIONS;
            }
        };
        passAction.addActionListener(l -> game.resolveAction(new PassAction()));
        actionMenu.add(passAction);

        final MenuItem darklingConvertAction = new ActionMenuItem("Darklings SH Conversion") {
            @Override
            public boolean canExecute(Game game) {
                return game.getCurrentPlayer().getPendingActions().contains(Player.PendingType.CONVERT_W2P);
            }
        };
        darklingConvertAction.addActionListener(l -> {
            final int workers = game.getCurrentPlayer().getWorkers();
            final String[] choices = IntStream.range(0, Math.min(3, workers) + 1).boxed().map(Object::toString).toArray(String[]::new);
            final int response = JOptionPane.showOptionDialog(game, "Convert W to P...", "Darklings SH Conversion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, null);
            if (response >= 0 && response < choices.length) {
                game.resolveAction(new DarklingsConvertAction(response));
            }
        });
        actionMenu.add(darklingConvertAction);

        final MenuBar menuBar = new MenuBar();
        menuBar.add(actionMenu);

        final int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        final int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        final JScrollPane jsp = new JScrollPane(game, v, h);

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
                switch (game.phase) {
                    case ACTIONS:
                    case CONFIRM_ACTION:
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_ENTER -> {
                                final Set<Player.PendingType> skippableActions = game.getCurrentPlayer().getSkippablePendingActions();
                                if (game.getCurrentPlayer().getPendingActions().isEmpty() || !skippableActions.isEmpty()) {
                                    int option = JOptionPane.OK_OPTION;
                                    if (!skippableActions.isEmpty()) {
                                        final String pending = "Are you sure you want to skip: " + skippableActions.stream().map(Player.PendingType::getDescription).collect(Collectors.joining(" / ")) + "?";
                                        option = JOptionPane.showConfirmDialog(null, pending, "Skip Action?", JOptionPane.OK_CANCEL_OPTION);
                                    }
                                    if (option == JOptionPane.OK_OPTION) {
                                        game.confirmTurn();
                                    }
                                }
                            }
                            case KeyEvent.VK_C -> game.resolveAction(new ConvertAction(Resources.c1, 0, 0, 0, 0));
                            case KeyEvent.VK_W -> game.resolveAction(new ConvertAction(Resources.w1, 0, 0, 0, 0));
                            case KeyEvent.VK_P -> game.resolveAction(new ConvertAction(Resources.p1, 0, 0, 0, 0));
                            case KeyEvent.VK_A -> game.resolveAction(new ConvertAction(Resources.zero, 0, 0, 1, 0));
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
            final Menu actionMenu = new Menu("Actions");
            final GameData test = new GameData(file);
            final JFrame frame = new JFrame();
            final Game game = new Game(frame, baseMapData, test, actionMenu);
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