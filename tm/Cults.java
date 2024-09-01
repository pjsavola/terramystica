package tm;

import tm.action.PriestToCultAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class Cults extends JPanel {

    private final List<Player> players;
    private final Player[][] cultPriests = new Player[4][4];
    private final static Font font = new Font("Arial", Font.PLAIN, 12);
    private final static Font cultFont = new Font("Arial", Font.PLAIN, 9);

    public Cults(Game game, List<Player> players) {
        this.players = players;
        addMouseListener(new MouseListener() {

            private List<Integer> getSpotOptions(int cult) {
                final List<Integer> result = new ArrayList<>();
                if (isCultSpotFree(cult, 3)) result.add(3);
                if (isCultSpotFree(cult, 2)) result.add(2);
                result.add(1);
                return result;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (game.phase != Game.Phase.ACTIONS) return;

                final int px = e.getX();
                final int py = e.getY();
                if (py >= 540 && e.getButton() == MouseEvent.BUTTON1) {
                    for (int cult = 0; cult < 4; ++cult) {
                        if (px < (cult + 1) * 50) {
                            game.resolveAction(new PriestToCultAction(cult, getSpotOptions(cult).get(0)));
                            break;
                        }
                    }
                } else if (py <= 20 || (py >= 540 && e.getButton() == MouseEvent.BUTTON3)) {
                    for (int cult = 0; cult < 4; ++cult) {
                        if (px < (cult + 1) * 50) {
                            final List<Integer> options = getSpotOptions(cult);
                            final String[] choices = options.stream().map(Object::toString).toArray(String[]::new);
                            final int response = JOptionPane.showOptionDialog(game, "Send P to " + getCultName(cult) + " for...", "Send Priest", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, null);
                            if (response >= 0 && response < options.size()) {
                                game.resolveAction(new PriestToCultAction(cult, options.get(response)));
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    public void reset() {
        for (Player[] cultPriest : cultPriests) {
            Arrays.fill(cultPriest, null);
        }
    }

    public static Color getCultColor(int cult) {
        return switch (cult) {
            case 0 -> new Color(0xFF8888);
            case 1 -> new Color(0xBBBBFF);
            case 2 -> new Color(0xBB9977);
            case 3 -> new Color(0xFFFFFF);
            default -> throw new RuntimeException("Invalid cult " + cult);
        };
    }

    public static String getCultName(int cult) {
        return switch (cult) {
            case 0 -> "FIRE";
            case 1 -> "WATER";
            case 2 -> "EARTH";
            case 3 -> "AIR";
            default -> throw new RuntimeException("Invalid cult " + cult);
        };
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(getCultColor(0));
        g.fillRect(0, 0, 50, 560);
        g.setColor(getCultColor(1));
        g.fillRect(50, 0, 50, 560);
        g.setColor(getCultColor(2));
        g.fillRect(100, 0, 50, 560);
        g.setColor(getCultColor(3));
        g.fillRect(150, 0, 50, 560);

        g.setFont(font);
        g.setColor(Color.BLACK);
        final FontMetrics metrics = g.getFontMetrics();
        for (int i = 0; i < 4; ++i) {
            int x = i * 50;
            final int w = metrics.stringWidth(getCultName(i));
            final int h = metrics.getHeight();
            g.setColor(Color.BLACK);
            g.drawString(getCultName(i), x + 3, h);
            for (int step = 10; step >= 0; --step) {
                int y = 18 + 48 * (10 - step);
                g.setFont(font);
                g.drawString(Integer.toString(step), x + 3, y + h);
                final List<Player> playersAtStep = new ArrayList<>();
                for (Player player : players) {
                    if (player.getCultSteps(i) == step && player.getFaction() != null) {
                        playersAtStep.add(player);
                    }
                }
                int dx = 1;
                int dy = 0;
                for (Player player : playersAtStep) {
                    final Hex.Type type = player.getHomeType();
                    g.setColor(type.getBuildingColor());
                    g.fillOval(x + dx, y + dy + h + 5, 12, 12);
                    g.setColor(type.getFontColor());
                    g.setFont(cultFont);
                    final String symbol = player.getFaction().getName().substring(0, 1);
                    final FontMetrics cultFontMetrics = g.getFontMetrics();
                    final int cultFontWidth = cultFontMetrics.stringWidth(symbol);
                    final int cultFontHeight = cultFontMetrics.getHeight();
                    g.drawString(symbol, x + dx + 6 - cultFontWidth / 2, y + dy + h + 8 + cultFontHeight / 2);
                    g.setColor(Color.BLACK);
                    g.drawOval(x + dx, y + dy + h + 5, 12, 12);
                    dx += 12;
                    if (dx > 50) {
                        dy += 12;
                        dx = 1;
                    }
                }
            }
            for (int j = 0; j < 4; ++j) {
                g.setFont(font);
                if (cultPriests[i][j] == null) {
                    g.setColor(Color.BLACK);
                    g.drawString(j == 0 ? "3" : "2", x + 3 + 10 * j, 540 + h);
                } else {
                    g.setColor(cultPriests[i][j].getHomeType().getBuildingColor());
                    g.drawString("P", x + 3 + 10 * j, 540 + h);
                }
            }
        }
        g.setColor(Color.BLACK);
        g.drawLine(0, 62, 200, 62);
        g.drawLine(0, 65, 200, 65);
        g.drawLine(0, 68, 200, 68);
        g.drawLine(0, 209, 200, 209);
        g.drawLine(0, 212, 200, 212);
        g.drawLine(0, 305, 200, 305);
        g.drawLine(0, 308, 200, 308);
        g.drawLine(0, 404, 200, 404);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 560);
    }

    public boolean isCultSpotFree(int cult, int amount) {
        if (amount == 3) {
            return cultPriests[cult][0] == null;
        } else if (amount == 2) {
            return cultPriests[cult][1] == null || cultPriests[cult][2] == null || cultPriests[cult][3] == null;
        }
        return true;
    }

    public int getFreeCultSpotCount(int cult, int amount) {
        if (amount == 3) {
            return cultPriests[cult][0] == null ? 1 : 0;
        } else if (amount == 2) {
            int count = 0;
            for (int i = 1; i <= 3; ++i) {
                if (cultPriests[cult][i] == null) ++count;
            }
            return count;
        }
        return Integer.MAX_VALUE;
    }

    public void sendPriestToCult(Player player, int cult, int amount) {
        if (amount == 3 && cultPriests[cult][0] != null)
            throw new RuntimeException("Cult spot of 3 already occupied");

        if (amount == 2) {
            boolean cultFound = false;
            for (int i = 1; i < 4; ++i) {
                if (cultPriests[cult][i] == null) {
                    cultPriests[cult][i] = player;
                    cultFound = true;
                    break;
                }
            }
            if (!cultFound) {
                throw new RuntimeException("No cult spots of 2 remaining");
            }
        } else if (amount == 3) {
            cultPriests[cult][0] = player;
        }
        player.sendPriestToCult(cult, amount);
    }

    public static int selectCult(JPanel panel, int steps, boolean force) {
        final String[] choices = { getCultName(3), getCultName(2), getCultName(1), getCultName(0) };
        int response;
        do {
            response = JOptionPane.showOptionDialog(panel, "Gain +" + steps + " cult in...", "Choose cult", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, null);
        } while (force && (response < 0 || response >= choices.length));
        return 3 - response;
    }
}
