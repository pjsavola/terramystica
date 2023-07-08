package tm;

import java.awt.*;
import java.util.List;

public class Bridge {

    private final Player owner;
    private final Hex hex1;
    private final Hex hex2;

    public Bridge(Player owner, Hex hex1, Hex hex2) {
        this.owner = owner;
        this.hex1 = hex1;
        this.hex2 = hex2;
    }

    public Player getOwner() {
        return owner;
    }

    public Hex getHex1() {
        return hex1;
    }

    public Hex getHex2() {
        return hex2;
    }

    public Hex getOtherEnd(Hex hex) {
        if (hex1 == hex) return hex2;
        if (hex2 == hex) return hex1;
        return null;
    }

    public void draw(Graphics2D g, int x1, int y1, int x2, int y2) {
        final Stroke oldStroke = g.getStroke();
        final Color oldColor = g.getColor();

        final int startX = x1 + (x2 - x1) / 3;
        final int startY = y1 + (y2 - y1) / 3;
        final int endX = x1 + (x2 - x1) * 2 / 3;
        final int endY = y1 + (y2 - y1) * 2 / 3;

        g.setStroke(new BasicStroke(8, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g.setColor(Color.BLACK);
        g.drawLine(startX, startY, endX, endY);
        g.setStroke(new BasicStroke(6, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g.setColor(owner.getHomeType().getHexColor());
        g.drawLine(startX, startY, endX, endY);

        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }
}
