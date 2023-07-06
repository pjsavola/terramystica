package tm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Grid extends JPanel {

    private final Hex[][] map;
    private final Point[][] points;
    private final int radius = 40;
    private final Dimension size;
    private final Game game;
    private final List<Bridge> bridges = new ArrayList<>();

    public Grid(Game game, String[] mapData) {
        this.game = game;
        final double halfWidth = Math.sqrt(3) / 2 * radius;
        map = new Hex[mapData.length][];
        points = new Point[mapData.length][];
        for (int row = 0; row < mapData.length; ++row) {
            String[] cols = mapData[row].split(",");
            map[row] = new Hex[cols.length];
            points[row] = new Point[cols.length];
            int number = 0;
            for (int col = 0; col < cols.length; ++col) {
                final Hex.Type type = Main.getType(cols[col]);
                final String id;
                if (type == Hex.Type.WATER) {
                    id = "";
                } else {
                    id = "" + (char) ('A' + row) + (number + 1);
                    ++number;
                }
                final int x = 2 + (int) (((row % 2 == 0 ? 1 : 2) + col * 2) * halfWidth);
                final int y = 2 + (int) ((1 + row * 1.5) * radius);
                points[row][col] = new Point(x, y);
                map[row][col] = new Hex(id, type);
            }
        }

        for (int row = 0; row < map.length; ++row) {
            for (int col = 0; col < map[row].length; ++col) {
                final List<Hex> neighbors = new ArrayList<>();
                if (col > 0) {
                    neighbors.add(map[row][col - 1]);
                }
                if (col < map[row].length - 1) {
                    neighbors.add(map[row][col + 1]);
                }
                if (row > 0) {
                    if (col < map[row - 1].length) {
                        neighbors.add(map[row - 1][col]);
                    }
                    final int otherCol = col + (row % 2 == 0 ? -1 : 1);
                    if (otherCol >= 0 && otherCol < map[row - 1].length) {
                        neighbors.add(map[row - 1][otherCol]);
                    }
                }
                if (row < map.length - 1) {
                    if (col < map[row + 1].length) {
                        neighbors.add(map[row + 1][col]);
                    }
                    final int otherCol = col + (row % 2 == 0 ? -1 : 1);
                    if (otherCol >= 0 && otherCol < map[row + 1].length) {
                        neighbors.add(map[row + 1][otherCol]);
                    }
                }
                map[row][col].setNeighbors(neighbors);
            }
        }

        final int width = (int) (map[0].length * halfWidth * 2 + 4);
        final int height = (int) (((map.length - 1) * 1.5 + 2) * radius + 4);
        size = new Dimension(width, height);

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int minDistSq = Integer.MAX_VALUE;
                int selectedRow = 0;
                int selectedCol = 0;
                for (int row = 0; row < map.length; ++row) {
                    for (int col = 0; col < map[row].length; ++col) {
                        final Point p = points[row][col];
                        final int dx = p.x - e.getX();
                        final int dy = p.y - e.getY();
                        final int distSq = dx * dx + dy * dy;
                        if (distSq < minDistSq) {
                            minDistSq = distSq;
                            selectedRow = row;
                            selectedCol = col;
                        }
                    }
                }
                if (minDistSq < radius * radius) {
                    game.hexClicked(selectedRow, selectedCol);
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

    public void reset(String[] mapData) {
        for (int row = 0; row < mapData.length; ++row) {
            String[] cols = mapData[row].split(",");
            for (int col = 0; col < cols.length; ++col) {
                final Hex.Type type = Main.getType(cols[col]);
                map[row][col].reset(type);
            }
        }
        bridges.clear();
    }

    public Hex getHex(int row, int col) {
        if (row > map.length) return null;
        if (col > map[row].length) return null;
        return map[row][col];
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    public void paint(Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;

        for (Bridge bridge : bridges) {
            Point p1 = null;
            Point p2 = null;
            for (int row = 0; row < map.length; ++row) {
                for (int col = 0; col < map[row].length; ++col) {
                    if (bridge.getHex1() == map[row][col]) {
                        p1 = points[row][col];
                    }
                    if (bridge.getHex2() == map[row][col]) {
                        p2 = points[row][col];
                    }
                }
            }
            bridge.draw(g2d, p1.x, p1.y, p2.x, p2.y);
        }

        for (int row = 0; row < map.length; ++row) {
            for (int col = 0; col < map[row].length; ++col) {
                final Point p = points[row][col];
                map[row][col].draw(g2d, p.x, p.y, radius);
            }
        }
    }

    public void addBridge(Bridge bridge) {
        bridges.add(bridge);
    }
}
