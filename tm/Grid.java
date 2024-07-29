package tm;

import tm.faction.Mermaids;
import tm.faction.Riverwalkers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Grid extends JPanel {

    private final Hex[][] map;
    private final Point[][] points;
    private final int radius = 40;
    private final Dimension size;
    private final List<Bridge> bridges = new ArrayList<>();
    private final List<Hex> allHexes = new ArrayList<>();

    public Grid(Game game, String[] mapData) {
        final double halfWidth = Math.sqrt(3) / 2 * radius;
        map = new Hex[mapData.length][];
        points = new Point[mapData.length][];
        int waterCount = 0;
        final int firstRowLength = mapData[0].length() - (mapData[0].endsWith(",N") ? 2 : 0);
        final int secondRowLength = mapData[1].length() - (mapData[1].endsWith(",N") ? 2 : 0);
        final int rowDelta = firstRowLength > secondRowLength ? 0 : 1;
        for (int row = 0; row < mapData.length; ++row) {
            String usedRow = mapData[row];
            if (usedRow.endsWith(",N")) usedRow = usedRow.substring(0, usedRow.length() - 2);
            String[] cols = usedRow.split(",");
            map[row] = new Hex[cols.length];
            points[row] = new Point[cols.length];
            int number = 0;
            for (int col = 0; col < cols.length; ++col) {
                final Hex.Type type = JMystica.getType(cols[col]);
                final String id;
                if (type == Hex.Type.WATER) {
                    id = "r" + (waterCount++);
                } else {
                    id = "" + (char) ('A' + row + rowDelta) + (number + 1);
                    ++number;
                }
                final int x = 2 + (int) ((((row + rowDelta) % 2 == 0 ? 1 : 2) + col * 2) * halfWidth);
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
                    final int otherCol = col + ((row + rowDelta) % 2 == 0 ? -1 : 1);
                    if (otherCol >= 0 && otherCol < map[row - 1].length) {
                        neighbors.add(map[row - 1][otherCol]);
                    }
                }
                if (row < map.length - 1) {
                    if (col < map[row + 1].length) {
                        neighbors.add(map[row + 1][col]);
                    }
                    final int otherCol = col + ((row + rowDelta) % 2 == 0 ? -1 : 1);
                    if (otherCol >= 0 && otherCol < map[row + 1].length) {
                        neighbors.add(map[row + 1][otherCol]);
                    }
                }
                map[row][col].setNeighbors(neighbors);
            }
        }

        for (Hex[] hexes : map) {
            Collections.addAll(allHexes, hexes);
        }

        final int width = (int) (map[rowDelta].length * halfWidth * 2 + 4);
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
                    game.hexClicked(selectedRow, selectedCol, e.getButton());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        new Thread(() -> {
            while (true) {
                try {
                    boolean changes = false;
                    Thread.sleep(50);
                    for (Hex hex : allHexes) {
                        final int maxAlpha = hex.getType().getMaxHighlightAlpha();
                        int alpha = hex.highlightAlpha;
                        if (hex.highlight) {
                            alpha = Math.min(maxAlpha, alpha + maxAlpha / 16);
                        } else {
                            alpha = Math.max(0, alpha - maxAlpha / 8);
                        }
                        if (alpha != hex.highlightAlpha || hex.highlight) changes = true;
                        hex.highlightAlpha = alpha;
                    }
                    if (changes) {
                        repaint();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void reset(String[] mapData) {
        for (int row = 0; row < mapData.length; ++row) {
            String usedRow = mapData[row];
            if (usedRow.endsWith(",N")) usedRow = usedRow.substring(0, usedRow.length() - 2);
            String[] cols = usedRow.split(",");
            for (int col = 0; col < cols.length; ++col) {
                final Hex.Type type = JMystica.getType(cols[col]);
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

    public List<Hex> getAllHexes() {
        return allHexes;
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

    public Set<Hex> getReachableTiles(Player player) {
        final Map<Hex, Integer> distances = new HashMap<>();
        final Deque<Hex> work = new ArrayDeque<>();
        final int shipping = player.getShipping();
        getAllHexes().stream().filter(h -> h.getStructure() != null && h.getType() == player.getHomeType()).forEach(h -> {
            work.add(h);
            distances.put(h, 0);
        });

        while (!work.isEmpty()) {
            final Hex hex = work.removeFirst();
            for (Hex neighbor : hex.getNeighbors()) {
                if (player.getFaction() instanceof Riverwalkers) {
                    if (neighbor.getType() != Hex.Type.WATER && hex.getType() != Hex.Type.WATER) {
                        continue;
                    }
                }
                if (!distances.containsKey(neighbor)) {
                    final int distance = distances.get(hex) + 1;
                    distances.put(neighbor, distance);
                    if (neighbor.getType() == Hex.Type.WATER) {
                        if (shipping >= distance) {
                            work.add(neighbor);
                        }
                    }
                }
            }
            for (Bridge bridge : bridges) {
                final Hex neighbor = bridge.getOtherEnd(hex);
                if (neighbor != null) {
                    if (!distances.containsKey(neighbor)) {
                        final int distance = distances.get(hex) + 1;
                        distances.put(neighbor, distance);
                    }
                }
            }
        }
        return distances.keySet().stream().filter(hex -> hex.getType() != Hex.Type.WATER).collect(Collectors.toSet());
    }

    public Set<Hex> getJumpableTiles(Player player) {
        final int range = player.getRange();
        if (range < 2) return Collections.emptySet();

        final Map<Hex, Integer> distances = new HashMap<>();
        final Deque<Hex> work = new ArrayDeque<>();
        getAllHexes().stream().filter(h -> h.getStructure() != null && h.getType() == player.getHomeType()).forEach(h -> {
            work.add(h);
            distances.put(h, 0);
        });

        final Set<Hex> jumpables = new HashSet<>();
        while (!work.isEmpty()) {
            final Hex hex = work.removeFirst();
            for (Hex neighbor : hex.getNeighbors()) {
                if (!distances.containsKey(neighbor)) {
                    final int distance = distances.get(hex) + 1;
                    distances.put(neighbor, distance);
                    if (range > distance) {
                        work.add(neighbor);
                    }
                    if (distance > 1 && neighbor.getType() != Hex.Type.WATER) {
                        jumpables.add(neighbor);
                    }
                }
            }
        }

        for (Bridge bridge : bridges) {
            final Integer d1 = distances.get(bridge.getHex1());
            final Integer d2 = distances.get(bridge.getHex2());
            if (d1 != null && d1 == 0) {
                jumpables.remove(bridge.getHex2());
            }
            if (d2 != null && d2 == 0) {
                jumpables.remove(bridge.getHex1());
            }
        }
        return jumpables;
    }

    int updateTowns(Player player) {
        final int requiredSize = player.hasFavor(5) ? 6 : 7;
        final Map<Hex, Integer> sizes = new HashMap<>();
        getAllHexes().stream().filter(h -> !h.town && h.getStructure() != null && h.getType() == player.getHomeType()).forEach(h -> {
            sizes.put(h, h.getStructureSize(player));
        });
        int newTowns = 0;
        while (!sizes.isEmpty()) {
            final Hex hex = sizes.keySet().iterator().next();
            int size = sizes.remove(hex);
            boolean nearTown = false;
            final Deque<Hex> work = new ArrayDeque<>();
            final Set<Hex> town = new HashSet<>();
            work.add(hex);
            while (!work.isEmpty()) {
                final Hex n = work.removeFirst();
                town.add(n);
                for (Hex neighbor : n.getNeighbors()) {
                    if (hasTown(neighbor, player)) {
                        nearTown = true;
                    }
                    final Integer sz = sizes.remove(neighbor);
                    if (sz != null) {
                        size += sz;
                        work.add(neighbor);
                    }
                }
                for (Bridge bridge : bridges) {
                    final Hex neighbor = bridge.getOtherEnd(n);
                    if (neighbor != null) {
                        if (hasTown(neighbor, player)) {
                            nearTown = true;
                        }
                        final Integer sz = sizes.remove(neighbor);
                        if (sz != null) {
                            size += sz;
                            work.add(neighbor);
                        }
                    }
                }
            }
            if (nearTown || (size >= requiredSize && (town.size() >= 4 || (town.size() >= 3 && town.stream().anyMatch(h -> h.getStructure() == Hex.Structure.SANCTUARY))))) {
                for (Hex n : town) {
                    n.town = true;
                }
                if (!nearTown) {
                    ++newTowns;
                }
            }
        }
        return newTowns;
    }

    public boolean canPlaceMermaidTown(Hex waterHex, Player player) {
        if (waterHex.getType() != Hex.Type.WATER || waterHex.town) return false;

        final int requiredSize = player.hasFavor(5) ? 6 : 7;
        final Map<Hex, Integer> sizes = new HashMap<>();
        getAllHexes().stream().filter(h -> !h.town && h.getStructure() != null && h.getType() == player.getHomeType()).forEach(h -> {
            sizes.put(h, h.getStructureSize(player));
        });
        int size = 0;
        final Deque<Hex> work = new ArrayDeque<>();
        work.add(waterHex);
        while (!work.isEmpty()) {
            final Hex n = work.removeFirst();
            for (Hex neighbor : n.getNeighbors()) {
                if (hasTown(neighbor, player)) {
                    return false;
                }
                final Integer sz = sizes.remove(neighbor);
                if (sz != null) {
                    size += sz;
                    work.add(neighbor);
                }
            }
            for (Bridge bridge : bridges) {
                final Hex neighbor = bridge.getOtherEnd(n);
                if (neighbor != null) {
                    if (hasTown(neighbor, player)) {
                        return false;
                    }
                    final Integer sz = sizes.remove(neighbor);
                    if (sz != null) {
                        size += sz;
                        work.add(neighbor);
                    }
                }
            }
        }
        return size >= requiredSize;
    }

    public void updateMermaidTown(Hex waterHex, Player player) {
        if (waterHex.getType() != Hex.Type.WATER) throw new RuntimeException("Invalid terrain for Mermaid town");

        final int requiredSize = player.hasFavor(5) ? 6 : 7;
        final Map<Hex, Integer> sizes = new HashMap<>();
        getAllHexes().stream().filter(h -> !h.town && h.getStructure() != null && h.getType() == player.getHomeType()).forEach(h -> {
            sizes.put(h, h.getStructureSize(player));
        });
        int size = 0;
        final Deque<Hex> work = new ArrayDeque<>();
        final Set<Hex> town = new HashSet<>();
        work.add(waterHex);
        while (!work.isEmpty()) {
            final Hex n = work.removeFirst();
            town.add(n);
            for (Hex neighbor : n.getNeighbors()) {
                if (hasTown(neighbor, player)) {
                    throw new RuntimeException("Cannot place mermaid town next to existing towns");
                }
                final Integer sz = sizes.remove(neighbor);
                if (sz != null) {
                    size += sz;
                    work.add(neighbor);
                }
            }
            for (Bridge bridge : bridges) {
                final Hex neighbor = bridge.getOtherEnd(n);
                if (neighbor != null) {
                    if (hasTown(neighbor, player)) {
                        throw new RuntimeException("Cannot place mermaid town next to existing towns");
                    }
                    final Integer sz = sizes.remove(neighbor);
                    if (sz != null) {
                        size += sz;
                        work.add(neighbor);
                    }
                }
            }
        }
        for (Hex hex : town) {
            hex.town = true;
        }
    }

    public Set<Hex> getBridgeNeighbors(Hex hex) {
        final Set<Hex> result = new HashSet<>();
        for (Bridge bridge : bridges) {
            final Hex other = bridge.getOtherEnd(hex);
            if (other != null) {
                result.add(other);
            }
        }
        return result;
    }

    private static boolean hasTown(Hex hex, Player player) {
        if (hex.town) {
            if (hex.getStructure() != null && hex.getType() == player.getHomeType()) return true;
            return hex.getType() == Hex.Type.WATER && player.getFaction() instanceof Mermaids;
        }
        return false;
    }

    Point getPoint(String id) {
        for (int row = 0; row < map.length; ++row) {
            for (int col = 0; col < map[row].length; ++col) {
                if (map[row][col].getId().equalsIgnoreCase(id)) {
                    return new Point(row, col);
                }
            }
        }
        return null;
    }

    private int getDistance(Hex hex1, Hex hex2) {
        int row1 = -1;
        int row2 = -1;
        int col1 = -1;
        int col2 = -1;
        for (int row = 0; row < map.length; ++row) {
            for (int col = 0; col < map[row].length; ++col) {
                if (map[row][col] == hex1) {
                    row1 = row;
                    col1 = col;
                } else if (map[row][col] == hex2) {
                    row2 = row;
                    col2 = col;
                }
            }
        }
        if (row1 == -1 || row2 == -1) {
            throw new RuntimeException("Couldn't find hexes");
        }
        return getDistance(row1, col1, row2, col2);
    }

    private int getDistance(int row1, int col1, int row2, int col2) {
        final int colDelta = Math.abs(col1 - col2);
        if (row1 == row2) {
            return colDelta;
        }
        final int rowDelta = Math.abs(row1 - row2);
        if (rowDelta % 2 == 0) {
            return rowDelta + Math.max(0, colDelta - rowDelta / 2);
        }
        // Move one step towards optimal direction which then enables usage of the above simple distance calculation cases.
        final int newRow2 = row2 + Integer.compare(row1, row2);
        final Hex hex2 = map[row2][col2];
        int minColDelta = Integer.MAX_VALUE;
        int newCol2 = -1;
        for (Hex n : hex2.getNeighbors()) {
            for (int col = 0; col < map[newRow2].length; ++col) {
                if (map[newRow2][col] == n) {
                    final int neighborColDelta = Math.abs(col1 - col);
                    if (neighborColDelta < minColDelta) {
                        minColDelta = neighborColDelta;
                        newCol2 = col;
                    }
                }
            }
        }
        if (newCol2 == -1) {
            throw new RuntimeException("Neighbor not found");
        }
        return getDistance(row1, col1, newRow2, newCol2) + 1;
    }

    public int getNetworkSize(Player player) {
        return getConnectedSets(player).stream().map(List::size).max(Integer::compareTo).orElse(0);
    }

    public int getMaxDistance(Player player) {
        return getConnectedSets(player).stream().map(s -> {
            int maxDistance = 0;
            for (int i = 0; i < s.size(); ++i) {
                for (int j = i + 1; j < s.size(); ++j) {
                    final int distance = getDistance(s.get(i), s.get(j));
                    if (distance > maxDistance) {
                        maxDistance = distance;
                    }
                }
            }
            return maxDistance;
        }).max(Integer::compareTo).orElse(0);
    }

    public int getSASHDistance(Player player) {
        return getConnectedSets(player).stream().map(s -> {
            final Hex saHex = s.stream().filter(h -> h.getStructure() == Hex.Structure.SANCTUARY).findAny().orElse(null);
            final Hex shHex = s.stream().filter(h -> h.getStructure() == Hex.Structure.STRONGHOLD).findAny().orElse(null);
            if (saHex != null && shHex != null) {
                return getDistance(saHex, shHex);
            }
            return 0;
        }).max(Integer::compareTo).orElse(0);
    }

    public int getEdgeCount(Player player) {
        return getConnectedSets(player).stream().map(s -> {
            int edgeCount = (int) s.stream().filter(hex -> {
                // Sides
                for (Hex[] hexes : map) {
                    if (hexes[0] == hex || hexes[hexes.length - 1] == hex) {
                        return true;
                    }
                }
                // Top
                for (int col = 0; col < map[0].length; ++col) {
                    if (map[0][col] == hex) {
                        return true;
                    }
                }
                // Bottom
                for (int col = 0; col < map[map.length - 1].length; ++col) {
                    if (map[map.length - 1][col] == hex) {
                        return true;
                    }
                }
                return false;
            }).count();
            return edgeCount;
        }).max(Integer::compareTo).orElse(0);
    }

    public int getClusterCount(Player player) {
        final List<Hex> mermaidTowns = new ArrayList<>();
        if (player.getFaction() instanceof Mermaids) {
            getAllHexes().stream().filter(h -> h.getType() == Hex.Type.WATER && h.town).forEach(mermaidTowns::add);
        }
        return getConnectedSets(player).stream().map(s -> {
            int clusters = 0;
            final Set<Hex> remaining = new HashSet<>(s);
            remaining.addAll(mermaidTowns);
            final Deque<Hex> work = new ArrayDeque<>();
            while (!remaining.isEmpty()) {
                final Iterator<Hex> it = remaining.iterator();
                work.add(it.next());
                it.remove();
                ++clusters;
                while (!work.isEmpty()) {
                    final Hex hex = work.removeFirst();
                    hex.getNeighbors().stream().filter(remaining::contains).forEach(n -> {
                        if (remaining.remove(n)) {
                            work.add(n);
                        }
                    });
                    for (Bridge bridge : bridges) {
                        final Hex neighbor = bridge.getOtherEnd(hex);
                        if (neighbor != null) {
                            if (remaining.remove(neighbor)) {
                                work.add(neighbor);
                            }
                        }
                    }
                }
            }
            return clusters;
        }).max(Integer::compareTo).orElse(0);
    }

    private List<List<Hex>> getConnectedSets(Player player) {
        final List<List<Hex>> connectedSets = new ArrayList<>();
        final Deque<Hex> work = new ArrayDeque<>();
        final int shipping = player.getShipping();
        final int range = player.getRange();
        final Set<Hex> visited = new HashSet<>();

        if (range == 1) {
            Hex a = allHexes.stream().filter(h -> !h.isEmpty() && h.getType() == player.getHomeType()).findAny().orElse(null);
            while (a != null) {
                final List<Hex> connectedSet = new ArrayList<>();
                work.add(a);
                visited.add(a);
                while (!work.isEmpty()) {
                    final Hex hex = work.removeFirst();
                    if (!hex.isEmpty() && hex.getType() == player.getHomeType()) {
                        connectedSet.add(hex);
                    }
                    final Deque<Hex> water = new ArrayDeque<>();
                    final Map<Hex, Integer> waterDistances = new HashMap<>();
                    hex.getNeighbors().stream().filter(n -> n.getType() == Hex.Type.WATER).forEach(n -> {
                        waterDistances.put(n, 1);
                        water.add(n);
                    });
                    while (!water.isEmpty()) {
                        final Hex waterHex = water.removeFirst();
                        final int distance = waterDistances.get(waterHex) + 1;
                        waterHex.getNeighbors().stream().filter(n -> n.getType() == Hex.Type.WATER && !waterDistances.containsKey(n)).forEach(n -> {
                            waterDistances.put(n, distance);
                            if (shipping >= distance) {
                                water.add(n);
                            }
                        });
                        waterHex.getNeighbors().stream().filter(n -> n.getType() == player.getHomeType() && !n.isEmpty()).forEach(n -> {
                            if (shipping >= distance - 1) {
                                if (visited.add(n)) {
                                    work.add(n);
                                }
                            }
                        });
                    }
                    hex.getNeighbors().stream().filter(n -> n.getType() == player.getHomeType() && !n.isEmpty()).forEach(n -> {
                        if (visited.add(n)) {
                            work.add(n);
                        }
                    });
                    for (Bridge bridge : bridges) {
                        final Hex neighbor = bridge.getOtherEnd(hex);
                        if (neighbor != null) {
                            if (visited.add(neighbor)) {
                                work.add(neighbor);
                            }
                        }
                    }
                }
                connectedSets.add(connectedSet);
                a = allHexes.stream().filter(h -> !h.isEmpty() && h.getType() == player.getHomeType()).filter(h -> !visited.contains(h)).findAny().orElse(null);
            }
        } else {
            Hex a = allHexes.stream().filter(h -> !h.isEmpty() && h.getType() == player.getHomeType()).findAny().orElse(null);
            while (a != null) {
                final List<Hex> connectedSet = new ArrayList<>();
                work.add(a);
                visited.add(a);
                while (!work.isEmpty()) {
                    final Hex hex = work.removeFirst();
                    if (!hex.isEmpty() && hex.getType() == player.getHomeType()) {
                        connectedSet.add(hex);
                    }
                    final Deque<Hex> other = new ArrayDeque<>();
                    final Map<Hex, Integer> otherDistances = new HashMap<>();
                    for (Hex n : hex.getNeighbors()) {
                        if (n.getType() == player.getHomeType() && !n.isEmpty()) {
                            if (visited.add(n)) {
                                work.add(n);
                            }
                        } else {
                            if (!otherDistances.containsKey(n)) {
                                otherDistances.put(n, 1);
                                other.add(n);
                            }
                        }
                    }
                    while (!other.isEmpty()) {
                        final Hex otherHex = other.removeFirst();
                        final int distance = otherDistances.get(otherHex) + 1;
                        for (Hex n : otherHex.getNeighbors()) {
                            if (n.getType() == player.getHomeType() && !n.isEmpty()) {
                                if (visited.add(n)) {
                                    work.add(n);
                                }
                            } else {
                                if (!otherDistances.containsKey(n)) {
                                    otherDistances.put(n, distance);
                                    if (range > distance) {
                                        other.add(n);
                                    }
                                }
                            }
                        }
                    }
                }
                connectedSets.add(connectedSet);
                a = allHexes.stream().filter(h -> !h.isEmpty() && h.getType() == player.getHomeType()).filter(h -> !visited.contains(h)).findAny().orElse(null);
            }
        }
        return connectedSets;
    }
}
