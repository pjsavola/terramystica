package tm;

import java.util.List;
import java.awt.*;

public class Hex {

    public enum Type {
        BLACK(new Color(0x444444), Color.BLACK, Color.LIGHT_GRAY),
        BROWN(new Color(0xBB9977), new Color(0x997755)),
        YELLOW(new Color(0xFFFF55), new Color(0xDDDD33)),
        RED(new Color(0xFF8888), new Color(0xDD6666)),
        GRAY(new Color(0xCCCCCC), new Color(0xAAAAAA)),
        GREEN(new Color(0x44EE44), new Color(0x22CC22)),
        BLUE(new Color(0x66BBFF), new Color(0x4499DD)),
        WATER(Color.WHITE);

        private final Color color;
        private final Color buildingColor;
        private final Color fontColor;
        Type(Color color, Color buildingColor, Color fontColor) {
            this.color = color;
            this.buildingColor = buildingColor;
            this.fontColor = fontColor;
        }

        Type(Color color, Color buildingColor) {
            this(color, buildingColor, Color.BLACK);
        }

        Type(Color color) {
            this(color, color);
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
    };

    private final static Font font = new Font("Arial", Font.BOLD, 12);
    private final Type type;
    private final String id;
    private Structure structure;
    private List<Hex> neighbors;

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

    public Structure getStructure() {
        return structure;
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

    public void draw(Graphics2D g, int x, int y, int radius) {
        if (type == Type.WATER)
            return;

        final int[] xpoints = new int[6];
        final int[] ypoints = new int[6];
        for (int i = 0; i < 6; ++i) {
            final double angle = i * Math.PI / 3 + Math.toRadians(270);
            xpoints[i] = (int) (x + Math.cos(angle) * radius + 0.5);
            ypoints[i] = (int) (y + Math.sin(angle) * radius + 0.5);
        }

        final Stroke oldStroke = g.getStroke();
        final Color oldColor = g.getColor();

        g.setColor(type.color);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

        g.fillPolygon(xpoints, ypoints, 6);

        g.setColor(Color.BLACK);
        g.drawPolygon(xpoints, ypoints, 6);

        g.setColor(type.getFontColor());
        g.setFont(font);
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
                final int[] shapey = { y - 10, y - 20, y, y - 10, y, y, y + 10, y + 10 };
                g.setColor(type.getBuildingColor());
                g.fillPolygon(shapex, shapey, 7);
                g.setColor(type.getFontColor());
                g.drawPolygon(shapex, shapey, 7);
            } else if (structure == Structure.TEMPLE) {
                g.setColor(type.getBuildingColor());
                g.fillOval(x - 10, y - 10, 20, 20);
                g.setColor(type.getFontColor());
                g.drawOval(x - 10, y - 10, 20, 20);
            } else if (structure == Structure.STRONGHOLD) {
                g.setColor(type.getBuildingColor());
                g.fillRect(x - 20, y - 20, 40, 40);
                g.setColor(type.getFontColor());
                g.drawRect(x - 20, y - 20, 40, 40);
            } else if (structure == Structure.SANCTUARY) {
                g.setColor(type.getBuildingColor());
                g.fillRoundRect(x - 20, y - 10, 40, 20, 10, 10);
                g.setColor(type.getFontColor());
                g.drawRoundRect(x - 20, y - 10, 40, 20, 10, 10);
            } else {
                throw new RuntimeException("Invalid structure");
            }
        }

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }
}
