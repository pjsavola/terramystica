package tm;

import tm.faction.Yetis;

import java.util.List;
import java.awt.*;

public class Hex {

    public enum Type {
        BLACK(new Color(0x444444), Color.BLACK, 96, Color.LIGHT_GRAY),
        BROWN(new Color(0xBB9977), new Color(0x997755), 48),
        YELLOW(new Color(0xFFFF55), new Color(0xDDDD33), 32),
        RED(new Color(0xFF8888), new Color(0xDD6666), 40),
        GRAY(new Color(0xCCCCCC), new Color(0xAAAAAA), 40),
        GREEN(new Color(0x44EE44), new Color(0x22CC22), 48),
        BLUE(new Color(0x66BBFF), new Color(0x4499DD), 32),
        WATER(Color.WHITE),
        ICE(new Color(0xDDDDFF), new Color(0xBBBBDD), 32),
        VOLCANO(new Color(0xFFAA44), new Color(0xDD8822), 40),
        VARIABLE(new Color(0xCCDDCC), new Color(0xAABBAA), 32);

        private final Color color;
        private final Color buildingColor;
        private final Color fontColor;
        private final int maxHighlightAlpha;
        Type(Color color, Color buildingColor, int maxHighlightAlpha, Color fontColor) {
            this.color = color;
            this.buildingColor = buildingColor;
            this.fontColor = fontColor;
            this.maxHighlightAlpha = maxHighlightAlpha;
        }

        Type(Color color, Color buildingColor, int maxHighlightAlpha) {
            this(color, buildingColor, maxHighlightAlpha, Color.BLACK);
        }

        Type(Color color) {
            this(color, color, 0);
        }

        public Color getHexColor() {
            return color;
        }

        public Color getBuildingColor() {
            return buildingColor;
        }

        public Color getFontColor() {
            return fontColor;
        }

        public int getMaxHighlightAlpha() {
            return maxHighlightAlpha;
        }
    };

    public enum Structure {
        DWELLING(null),
        TRADING_POST(DWELLING),
        TEMPLE(TRADING_POST),
        STRONGHOLD(TRADING_POST),
        SANCTUARY(TEMPLE);

        private final Structure parent;

        private Structure(Structure parent) {
            this.parent = parent;
        }

        public Structure getParent() {
            return parent;
        }

        public String getName() {
            return switch (this) {
                case DWELLING -> "Dwelling";
                case TRADING_POST -> "Trading Post";
                case TEMPLE -> "Temple";
                case STRONGHOLD -> "Stronghold";
                case SANCTUARY -> "Sanctuary";
            };
        }

        public String getAbbrevation() {
            return switch (this) {
                case DWELLING -> "D";
                case TRADING_POST -> "TP";
                case TEMPLE -> "TE";
                case STRONGHOLD -> "SH";
                case SANCTUARY -> "SA";
            };
        }
    };

    final static long startTime = System.currentTimeMillis();
    final static Font font = new Font("Arial", Font.PLAIN, 13);
    final static Font townFont = new Font("Arial", Font.BOLD, 13);
    private Type type;
    private final String id;
    private Structure structure;
    private List<Hex> neighbors;
    boolean town;
    boolean highlight;
    int highlightAlpha;

    public Hex(String id, Type type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Structure getStructure() {
        return structure;
    }

    public boolean isEmpty() {
        return structure == null;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public List<Hex> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Hex> neighbors) {
        this.neighbors = neighbors;
    }

    public int getStructureSize(Player player) {
        return switch (getStructure()) {
            case DWELLING -> 1;
            case TRADING_POST -> 2;
            case TEMPLE -> 2;
            case STRONGHOLD -> player.getFaction() instanceof Yetis ? 4 : 3;
            case SANCTUARY -> player.getFaction() instanceof Yetis ? 4 : 3;
        };
    }

    public void reset(Type type) {
        this.type = type;
        structure = null;
        town = false;
        setHighlight(false);
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public void draw(Graphics2D g, int x, int y, int radius) {
        final Stroke oldStroke = g.getStroke();
        final Color oldColor = g.getColor();

        if (type == Type.WATER) {
            final int[] xpoints = new int[6];
            final int[] ypoints = new int[6];
            for (int i = 0; i < 6; ++i) {
                final double angle = i * Math.PI / 3 + Math.toRadians(270);
                xpoints[i] = (int) (x + Math.cos(angle) * (radius * 3 / 4) + 0.5);
                ypoints[i] = (int) (y + Math.sin(angle) * (radius * 3 / 4) + 0.5);
            }

            if (town) {
                g.setColor(Type.BLUE.getHexColor().brighter());
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
                g.fillPolygon(xpoints, ypoints, 6);
                g.setColor(Type.BLUE.getHexColor());
                g.drawPolygon(xpoints, ypoints, 6);
            } else if (highlight) {
                g.setColor(Type.BLUE.getHexColor());
                g.drawPolygon(xpoints, ypoints, 6);
            } else {
                return;
            }
            g.setColor(oldColor);
            g.setStroke(oldStroke);
            return;
        }

        final int[] xpoints = new int[6];
        final int[] ypoints = new int[6];
        for (int i = 0; i < 6; ++i) {
            final double angle = i * Math.PI / 3 + Math.toRadians(270);
            xpoints[i] = (int) (x + Math.cos(angle) * radius + 0.5);
            ypoints[i] = (int) (y + Math.sin(angle) * radius + 0.5);
        }

        g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g.setColor(type.color);
        g.fillPolygon(xpoints, ypoints, 6);

        int alpha = highlightAlpha;
        if (highlightAlpha > 0) {
            alpha = (int) (alpha * (1 + Math.sin((double) (System.currentTimeMillis() - startTime) / 200)) * 0.5);
        }
        g.setColor(new Color(type.getFontColor().getRed(), type.getFontColor().getGreen(), type.getFontColor().getBlue(), alpha));
        g.fillPolygon(xpoints, ypoints, 6);

        g.setColor(Color.BLACK);
        g.drawPolygon(xpoints, ypoints, 6);

        g.setColor(type.getFontColor());
        g.setFont(town ? townFont : font);
        final FontMetrics metrics = g.getFontMetrics();
        final int w = metrics.stringWidth(id);
        final int h = metrics.getHeight();
        g.drawString(id, x - w/2, y + (radius + h) / 2);

        if (structure != null) {
            g.setColor(type.getFontColor());
            if (structure == Structure.DWELLING) {
                final int[] shapex = { x - 10, x, x + 10, x + 10, x - 10 };
                final int[] shapey = { y, y - 10, y, y + 10, y + 10 };
                g.setColor(type.getBuildingColor());
                g.fillPolygon(shapex, shapey, 5);
                g.setColor(type.getFontColor());
                g.drawPolygon(shapex, shapey, 5);
            } else if (structure == Structure.TRADING_POST) {
                final int[] shapex = { x - 10, x, x + 10, x + 10, x + 20, x + 20, x - 10 };
                final int[] shapey = { y - 10, y - 20, y - 10, y, y, y + 10, y + 10 };
                g.setColor(type.getBuildingColor());
                g.fillPolygon(shapex, shapey, 7);
                g.setColor(type.getFontColor());
                g.drawPolygon(shapex, shapey, 7);
            } else if (structure == Structure.TEMPLE) {
                g.setColor(type.getBuildingColor());
                g.fillOval(x - 14, y - 14, 28, 28);
                g.setColor(type.getFontColor());
                g.drawOval(x - 14, y - 14, 28, 28);
            } else if (structure == Structure.STRONGHOLD) {
                g.setColor(type.getBuildingColor());
                g.fillRect(x - 16, y - 16, 32, 32);
                g.setColor(type.getFontColor());
                g.drawRect(x - 16, y - 16, 32, 32);
            } else if (structure == Structure.SANCTUARY) {
                g.setColor(type.getBuildingColor());
                g.fillRoundRect(x - 20, y - 14, 40, 28, 28, 28);
                g.setColor(type.getFontColor());
                g.drawRoundRect(x - 20, y - 14, 40, 28, 28, 28);
            } else {
                throw new RuntimeException("Invalid structure");
            }
        }

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    @Override
    public String toString() {
        return getId();
    }
}
