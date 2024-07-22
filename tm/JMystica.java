package tm;

import tm.action.Action;
import tm.faction.Giants;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class JMystica {
    public static final int maxReplayActionCount = 2000;
    public static final String gameFileExtension = "jtm";

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
        tests.put("tests/Petri21", new int[] {127, 66, 145, 96});
        tests.put("tests/Petri22", new int[] {174, 160});
        //tests.put("tests/Petri23", null); -- Dodgy resource manipulation!
        tests.put("tests/Petri24", null);
        tests.put("tests/Petri25", null);
        tests.put("tests/Petri26", null);
        tests.put("tests/Petri27", null);
        tests.put("tests/Petri28", null);
        tests.put("tests/Petri29", null);
        tests.put("tests/Petri30", null);
        tests.put("tests/Petri31", null);
        tests.put("tests/Petri32", null);
        tests.put("tests/Petri33", null);
        tests.put("tests/Petri34", null);
        tests.put("tests/Petri35", null);
        tests.put("tests/Petri36", null);
        tests.put("tests/Petri37", null);
        tests.put("tests/Petri38", null);
        tests.put("tests/Petri39", null);
        tests.put("tests/Petri40", null);
        tests.put("tests/Petri41", null);
        tests.put("tests/Petri42", null);
        tests.put("tests/Petri43", null);

        if (maxReplayActionCount >= 2000) {
            tests.forEach((file, vps) -> {
                if (!test(file, vps))
                    throw new RuntimeException("Test " + file + " failed!");
            });
        }

        final JFrame frame = new JFrame();
        final JPanel mainPanel = new JPanel();
        final WindowChanger windowChanger = new WindowChanger(frame, mainPanel);
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
        final JButton startButton = new JButton("New");
        final JButton loadButton = new JButton("Load");
        final JButton importButton = new JButton("Import");
        final JButton quitButton = new JButton("Quit");
        buttonPanel.add(startButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(importButton);
        buttonPanel.add(quitButton);
        startButton.addActionListener(l -> {
            final List<String> customMap = new ArrayList<>();
            final JDialog dialog = new JDialog(frame, "Game Settings");
            final JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(new JLabel("Map"));
            final Object[] mapOptions = {"Base", "F&I", "Fjords", "Loon Lakes", "Revised Base", "Custom ..."};
            final JComboBox mapChooser = new JComboBox(mapOptions);
            panel.add(mapChooser);
            mapChooser.addItemListener(new ItemListener() {
                private int previousIndex = -1;
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        for (int i = 0; i < mapOptions.length; ++i) {
                            if (mapOptions[i] == e.getItem()) {
                                previousIndex = i;
                                break;
                            }
                        }
                    } else if (e.getStateChange() == ItemEvent.SELECTED && "Custom ...".equals(e.getItem())) {
                        final JTextArea textArea = new JTextArea();
                        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
                        textArea.setRows(9);
                        final Object[] message = {"Map data:", textArea};
                        final int option = JOptionPane.showConfirmDialog(panel, message, "Insert map data", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                        if (option == JOptionPane.OK_OPTION) {
                            final String text = textArea.getText();
                            final String[] rows = text.split("\\n");
                            customMap.clear();
                            for (String row : rows) {
                                customMap.add(row.trim());
                            }
                        } else {
                            mapChooser.setSelectedIndex(previousIndex);
                        }
                    }
                }
            });
            panel.add(new JLabel("Faction Picks"));
            final JComboBox factionPickChooser = new JComboBox(new Object[] {"Manual", "Random"});
            panel.add(factionPickChooser);
            panel.add(new JLabel("Starting VPs"));
            final JComboBox startingVPsChooser = new JComboBox(new Object[] {"20", "Revised", "Auction"});
            panel.add(startingVPsChooser);
            panel.add(new JLabel("Setup random seed"));
            final JTextField seedChooser = new JTextField();
            panel.add(seedChooser);
            panel.add(new JLabel("Randomize order"));
            final JCheckBox randomizeOrderChooser = new JCheckBox();
            panel.add(randomizeOrderChooser);
            final List<JLabel> playerLabelList = new ArrayList<>();
            final List<JTextField> playerFieldList = new ArrayList<>();
            final JButton addButton = new JButton("Add player");
            final JButton removeButton = new JButton("Remove player");
            addButton.addActionListener(al -> {
                if (playerLabelList.size() < 7) {
                    final JLabel label = new JLabel("Player " + (playerLabelList.size() + 1));
                    final JTextField field = new JTextField();
                    playerLabelList.add(label);
                    playerFieldList.add(field);
                    panel.add(label, panel.getComponentCount() - 2);
                    panel.add(field, panel.getComponentCount() - 2);
                    panel.setLayout(new GridLayout(panel.getComponentCount() / 2, 2));
                    dialog.pack();
                }
            });
            removeButton.addActionListener(al -> {
                if (playerLabelList.size() > 1) {
                    panel.remove(playerLabelList.remove(playerLabelList.size() - 1));
                    panel.remove(playerFieldList.remove(playerFieldList.size() - 1));
                    panel.setLayout(new GridLayout(panel.getComponentCount() / 2, 2));
                    dialog.pack();
                }
            });
            panel.add(addButton);
            panel.add(removeButton);
            for (int i = 0; i < 4; ++i) {
                final JLabel label = new JLabel("Player " + (i + 1));
                final JTextField field = new JTextField();
                playerLabelList.add(label);
                playerFieldList.add(field);
                panel.add(label);
                panel.add(field);
            }
            final JButton start = new JButton("Start!");
            start.setForeground(Color.RED);
            final JButton cancel = new JButton("Cancel");
            panel.add(start);
            panel.add(cancel);
            start.addActionListener(el -> {
                final int seed;
                if (!seedChooser.getText().isEmpty()) {
                    try {
                        seed = Integer.parseInt(seedChooser.getText().trim());
                    } catch (NumberFormatException ex) {
                        final String input = "\"" + seedChooser.getText() + "\"";
                        JOptionPane.showMessageDialog(panel, "Invalid number: " + input, "Error", JOptionPane.ERROR_MESSAGE, null);
                        return;
                    }
                } else {
                    seed = new Random().nextInt();
                }
                final String[] mapData;
                if (mapChooser.getSelectedIndex() == 5 && !customMap.isEmpty()) {
                    mapData = customMap.toArray(new String[0]);
                } else {
                    final String mapName = (String) mapOptions[mapChooser.getSelectedIndex()];
                    mapData = MapData.mapsByName.get(mapName).getData();
                }
                if (randomizeOrderChooser.isSelected()) {
                    Collections.shuffle(playerFieldList);
                }
                final Set<String> names = new HashSet<>();
                for (int i = 0; i < playerFieldList.size(); ++i) {
                    if (playerFieldList.get(i).getText().isEmpty()) {
                        playerFieldList.get(i).setText("Player " + (i + 1));
                    }
                    if (!names.add(playerFieldList.get(i).getText())) {
                        JOptionPane.showMessageDialog(panel, "Please choose unique or empty names", "Error", JOptionPane.ERROR_MESSAGE, null);
                        return;
                    }
                }
                final List<String> playerNames = playerFieldList.stream().map(JTextComponent::getText).toList();
                final GameData gameData = new GameData(playerNames, seed);
                gameData.mapData = mapData;
                gameData.useRevisedStartingVPs = startingVPsChooser.getSelectedIndex() == 1;
                gameData.useAuction = startingVPsChooser.getSelectedIndex() == 2;
                gameData.chooseFactions = factionPickChooser.getSelectedIndex() == 0;
                Game.open(frame, gameData);
                dialog.setVisible(false);
            });
            cancel.addActionListener(el -> dialog.setVisible(false));
            panel.setLayout(new GridLayout(panel.getComponentCount() / 2, 2));
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(mainPanel);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
        loadButton.addActionListener(l -> {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to load");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(new FileNameExtensionFilter("JMystica Game", gameFileExtension));
            fileChooser.setFileSystemView(new JtmFileSystemView());
            final int userSelection = fileChooser.showOpenDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                final File fileToLoad = fileChooser.getSelectedFile();
                try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(fileToLoad))) {
                    final GameData gameData = (GameData) stream.readObject();
                    int actionCount = stream.readInt();
                    final List<Action> actions = new ArrayList<>(actionCount);
                    while (actionCount-- > 0) {
                        final Action action = (Action) stream.readObject();
                        actions.add(action);
                    }
                    gameData.history = actions;
                    Game.open(frame, gameData);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        importButton.addActionListener(l -> {
            try {
                final int choice = JOptionPane.showOptionDialog(mainPanel, "Import from ...", "Choose import method", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] {"Help!", "File", "Clipboard"}, "Clipboard");
                switch (choice) {
                    case 0 ->
                            JOptionPane.showMessageDialog(mainPanel, "To import an existing game from terra.snellman.net,\nopen any game from http://terra.snellman.net in your browser.\nClick 'Load Full Log' button, select everything (Ctrl-A)\nand copy it to the clipboard (Ctrl-C).", "Import Instructions", JOptionPane.PLAIN_MESSAGE, null);
                    case 1 -> {
                        final JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                        final int returnValue = jfc.showOpenDialog(mainPanel);
                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            final File selectedFile = jfc.getSelectedFile();
                            Game.open(frame, new Scanner(selectedFile));
                        }
                    }
                    case 2 -> {
                        final String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                        Game.open(frame, new Scanner(data));
                    }
                    default -> {
                    }
                }
            } catch (Throwable e) {
                JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, null);
            }
        });
        quitButton.addActionListener(l -> frame.setVisible(false));
        buttonPanel.setBackground(Color.BLACK);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(imagePanel);
        mainPanel.add(buttonPanel);
        frame.setContentPane(mainPanel);
        frame.addWindowListener(windowChanger);
        //frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
    }

    public static boolean test(String file, int[] vpTargets) {
        try {
            final GameData test = new GameData(new Scanner(new File(file)));
            test.silentMode = true;
            final JFrame frame = new JFrame();
            try {
                final Game game = new Game(frame, test);
                final int[] vps = game.getVictoryPoints();
                if (game.validateVictoryPoints(vpTargets)) {
                    game.rewind();
                    return true;
                }
                System.err.println("Wrong vps: " + Arrays.stream(vps).mapToObj(String::valueOf).collect(Collectors.joining(",")));
                test.printAndClearLogs();
            } catch (Throwable e) {
                test.printAndClearLogs();
                throw e;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}