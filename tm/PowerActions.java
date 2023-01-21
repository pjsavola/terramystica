package tm;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PowerActions extends JPanel {

    private final boolean[] usedPowerActions;
    private final static Font font = new Font("Arial", Font.BOLD, 12);

    public PowerActions(boolean[] usedPowerActions) {
        this.usedPowerActions = usedPowerActions;
    }

    @Override
    public void paint(Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;

        final Stroke oldStroke = g2d.getStroke();
        final Color oldColor = g.getColor();

        final String[] texts = { "bridge", "P", "2W", "7C", "spd", "2 spd" };

        for (int act = 0; act < usedPowerActions.length; ++act) {
            drawPowerAction(g2d, act * 55, 0, texts[act], usedPowerActions[act]);
        }

        g.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    private static final int[] xpoints = { 2, 17, 32, 47, 47, 32, 17, 2 };
    private static final int[] ypoints = { 17, 2, 2, 17, 32, 47, 47, 32 };

    public static void drawPowerAction(Graphics2D g, int x, int y, String text, boolean used) {
        final int[] xpoints = new int[8];
        final int[] ypoints = new int[8];
        for (int i = 0; i < 8; ++i) {
            xpoints[i] = PowerActions.xpoints[i] + x;
            ypoints[i] = PowerActions.ypoints[i] + y;
        }
        g.setColor(used ? Color.LIGHT_GRAY : new Color(0xDDBB00));
        g.fillPolygon(xpoints, ypoints, 8);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g.drawPolygon(xpoints, ypoints, 8);
        g.setFont(font);
        final FontMetrics metrics = g.getFontMetrics();
        final int w = metrics.stringWidth(text);
        g.drawString(text, xpoints[0] + 23 - w/2, y + 27);
    }

    public static boolean actionClicked(int x, int y) {
        final int minX = Arrays.stream(xpoints).min().getAsInt();
        final int minY = Arrays.stream(ypoints).min().getAsInt();
        final int maxX = Arrays.stream(xpoints).max().getAsInt();
        final int maxY = Arrays.stream(ypoints).max().getAsInt();
        if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
            // Corners
            if (y - minY < 15 || maxY - y < 15) {
                return x - minX >= 15 && maxX - x >= 15;
            }
            return true;
        }
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(354, 49);
    }
}
