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
        final Menu actionMenu = new Menu("Actions");
        final Menu convertMenu = new Menu("Convert");
        final Menu advanceMenu = new Menu("Advance");

        final GameData test = new GameData(1, new Random().nextInt());
        //final GameData test = new GameData("tests/Petri01");

        final JFrame frame = new JFrame();
        final Game game = new Game(frame, baseMapData, test, new Menu[] { actionMenu, convertMenu, advanceMenu });

        new ActionMenuItem(game, convertMenu, "Convert ...") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION;
            }

            @Override
            protected void addListener(Game game) {
                addActionListener(l -> {
                    if (game.phase != Game.Phase.ACTIONS && game.phase != Game.Phase.CONFIRM_ACTION) return;

                    final boolean alchemists = game.getCurrentPlayer().getFaction() instanceof Alchemists;
                    final JTextField priestsToWorkers = new JTextField();
                    final JTextField workersToCoins = new JTextField();
                    final JTextField powerToPriests = new JTextField();
                    final JTextField powerToWorkers = new JTextField();
                    final JTextField powerToCoins = new JTextField();
                    final JTextField coinsToPoints = new JTextField();
                    final JTextField pointsToCoins = new JTextField();
                    Object[] message = {
                            "P -> W:", priestsToWorkers,
                            "W -> C:", workersToCoins,
                            "5 PW -> P", powerToPriests,
                            "3 PW -> W", powerToWorkers,
                            "1 PW -> C", powerToCoins,
                            "3 C -> VP", coinsToPoints,
                            "1 VP -> C", pointsToCoins
                    };
                    message = Arrays.stream(message).limit(alchemists ? 14 : 12).toArray();

                    int option = JOptionPane.showConfirmDialog(null, message, "Convert ...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            final int p2w = priestsToWorkers.getText().isEmpty() ? 0 : Integer.parseInt(priestsToWorkers.getText());
                            final int w2c = workersToCoins.getText().isEmpty() ? 0 : Integer.parseInt(workersToCoins.getText());
                            final int vp2c = pointsToCoins.getText().isEmpty() ? 0 : Integer.parseInt(pointsToCoins.getText());
                            final int pw2p = powerToPriests.getText().isEmpty() ? 0 : Integer.parseInt(powerToPriests.getText());
                            final int pw2w = powerToWorkers.getText().isEmpty() ? 0 : Integer.parseInt(powerToWorkers.getText());
                            final int pw2c = powerToCoins.getText().isEmpty() ? 0 : Integer.parseInt(powerToCoins.getText());
                            final int c2vp = coinsToPoints.getText().isEmpty() ? 0 : Integer.parseInt(coinsToPoints.getText());
                            Resources powerConversions = Resources.zero;
                            if (pw2p > 0) powerConversions = powerConversions.combine(Resources.fromPriests(pw2p));
                            if (pw2w > 0) powerConversions = powerConversions.combine(Resources.fromWorkers(pw2w));
                            if (pw2c > 0) powerConversions = powerConversions.combine(Resources.fromCoins(pw2c));
                            if (powerConversions != Resources.zero || p2w > 0 || w2c > 0 || vp2c > 0 || c2vp > 0) {
                                game.resolveAction(new ConvertAction(powerConversions, p2w, w2c, vp2c, c2vp));
                            }
                        } catch (NumberFormatException ex) {
                            final String input = ex.getMessage().substring(ex.getMessage().indexOf('"'));
                            JOptionPane.showConfirmDialog(null, "Invalid number: " + input, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null);
                        }
                    }
                });
            }
        };

        new ConvertMenuItem(game, convertMenu, "PW -> C", KeyEvent.VK_C, Resources.c1);
        new ConvertMenuItem(game, convertMenu, "3PW -> W", KeyEvent.VK_W, Resources.w1);
        new ConvertMenuItem(game, convertMenu, "5PW -> P", KeyEvent.VK_P, Resources.p1);
        new ActionMenuItem(game, convertMenu, "VP -> C", KeyEvent.VK_A) {
            @Override
            public boolean canExecute(Game game) {
                return (game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION) && game.getCurrentPlayer().canConvert(0, 0, 1, 0);
            }

            @Override
            protected void addListener(Game game) {
                game.resolveAction(new ConvertAction(Resources.zero, 0, 0, 1, 0));
            }
        };

        new ActionMenuItem(game, advanceMenu,"Advance ship") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS && game.getCurrentPlayer().canAdvanceShipping();
            }

            @Override
            protected void addListener(Game game) {
                addActionListener(l -> game.resolveAction(new AdvanceAction(false)));
            }
        };

        new ActionMenuItem(game, advanceMenu, "Advance dig") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS && game.getCurrentPlayer().canAdvanceDigging();
            }

            @Override
            protected void addListener(Game game) {
                addActionListener(l -> game.resolveAction(new AdvanceAction(true)));
            }
        };

        new ActionMenuItem(game, actionMenu, "Final Pass") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS && game.getRound() == 6;
            }

            @Override
            protected void addListener(Game game) {
                addActionListener(l -> game.resolveAction(new PassAction()));
            }
        };

        new ActionMenuItem(game, actionMenu, "Darklings SH Conversion") {
            @Override
            public boolean canExecute(Game game) {
                return game.getCurrentPlayer().getPendingActions().contains(Player.PendingType.CONVERT_W2P);
            }

            @Override
            protected void addListener(Game game) {
                addActionListener(l -> {
                    final int workers = game.getCurrentPlayer().getWorkers();
                    final String[] choices = IntStream.range(0, Math.min(3, workers) + 1).boxed().map(Object::toString).toArray(String[]::new);
                    final int response = JOptionPane.showOptionDialog(game, "Convert W to P...", "Darklings SH Conversion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, null);
                    if (response >= 0 && response < choices.length) {
                        game.resolveAction(new DarklingsConvertAction(response));
                    }
                });
            }
        };

        final MenuBar menuBar = new MenuBar();
        menuBar.add(convertMenu);
        menuBar.add(advanceMenu);
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
            final Game game = new Game(frame, baseMapData, test, null);
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