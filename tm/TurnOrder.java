package tm;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TurnOrder extends JPanel {

    private final Game game;
    private final List<Player> turnOrder;
    private final List<Player> nextTurnOrder;
    private final static Font font = new Font("Arial", Font.BOLD, 16);
    private final static int circleRadius = 20;
    private final static int topMargin = 20;

    public TurnOrder(Game game, List<Player> turnOrder, List<Player> nextTurnOrder) {
        this.game = game;
        this.turnOrder = turnOrder;
        this.nextTurnOrder = nextTurnOrder;
    }

    @Override
    public void paint(Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;

        final Stroke oldStroke = g2d.getStroke();
        final Color oldColor = g.getColor();

        g.setColor(Color.BLACK);
        g.setFont(font);

        switch (game.phase) {
            case INITIAL_DWELLINGS -> drawPlayers(g, 0, topMargin, "Setup", turnOrder);
            case INITIAL_BONS -> drawPlayers(g, 0, topMargin, "Pick Bon", turnOrder);
            case ACTIONS -> {
                int x = drawPlayers(g, 0, topMargin, "Active", turnOrder) + 10;
                drawPlayers(g, x, topMargin, "Passed", nextTurnOrder);
            }
        };
        g.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    private static int drawPlayers(Graphics g, int x, int y, String section, List<Player> players) {
        final FontMetrics fontMetrics = g.getFontMetrics();
        g.drawString(section, x, y + 16);
        x += fontMetrics.stringWidth(section) + 10;
        for (Player player : players) {
            final Hex.Type type = player.getHomeType();
            g.setColor(type.getBuildingColor());
            g.fillOval(x, y, circleRadius, circleRadius);
            g.setColor(type.getFontColor());
            final String symbol = player.getFaction().getName().substring(0, 1);
            g.drawString(symbol, x + (circleRadius - fontMetrics.stringWidth(symbol) + 1) / 2, y + 16);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, circleRadius, circleRadius);
            x += 24;
        }
        return x;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(354, 44);
    }
}
