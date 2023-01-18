package tm;

import java.awt.*;

public abstract class Bons {
    public static Resources getBonIncome(int bon) {
        return switch (bon) {
            case 1 -> Resources.c2;
            case 2 -> Resources.c4;
            case 3 -> Resources.c6;
            case 4 -> Resources.pw3;
            case 5 -> Resources.w1pw3;
            case 6 -> Resources.w2;
            case 7 -> Resources.w1;
            case 8 -> Resources.p1;
            case 9 -> Resources.c2;
            case 10 -> Resources.pw3;
            default -> throw new RuntimeException("Invalid bonus tile: " + bon);
        };
    }

    public static void drawBon(Graphics g, int x, int y, int bon, int coins) {
        g.setColor(new Color(0xEEDDBB));
        g.fillRect(x, y, 100, 100);

        g.setColor(Color.GRAY);
        g.drawLine(x + 5, y + 20, x + 95, y + 20);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, 100, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        String title = "BON" + bon;
        if (coins > 0) {
            title += "[" + coins + "c]";
        }
        g.drawString(title, x + 3, y + 14);

        Graphics2D g2d = (Graphics2D) g;
        int dx = 3;
        int dy = 30;
        if (bon == 1) {
            final Color oldColor = g.getColor();
            final Stroke oldStroke = g2d.getStroke();
            PowerActions.drawPowerAction(g2d, x + dx, y + dy, "spd", false);
            g.setColor(oldColor);
            g2d.setStroke(oldStroke);
            dx += 49;
        } else if (bon == 2) {
            final Color oldColor = g.getColor();
            final Stroke oldStroke = g2d.getStroke();
            PowerActions.drawPowerAction(g2d, x + dx, y + dy, "cult", false);
            g.setColor(oldColor);
            g2d.setStroke(oldStroke);
            dx += 49;
        } else if (bon == 6) {
            g.drawString("pass-vp:SH*4", x + dx, y + dy + 8);
            dy += 14;
            g.drawString("pass-vp:SA*4", x + dx, y + dy + 8);
            dy += 14;
        } else if (bon == 7) {
            g.drawString("pass-vp:TP*3", x + dx, y + dy + 8);
            dy += 14;
        } else if (bon == 9) {
            g.drawString("pass-vp:D*2", x + dx, y + dy + 8);
            dy += 14;
        }
        Resources r = getBonIncome(bon);
        if (r.coins > 0) {
            g.drawString("+" + r.coins + " C", x + dx, y + dy + 8);
            dy += 14;
        }
        if (r.workers > 0) {
            g.drawString("+" + r.workers + " W", x + dx, y + dy + 8);
            dy += 14;
        }
        if (r.priests > 0) {
            g.drawString("+" + r.priests + " P", x + dx, y + dy + 8);
            dy += 14;
        }
        if (r.power > 0) {
            g.drawString("+" + r.power + " PW", x + dx, y + dy + 8);
            dy += 14;
        }
        if (bon == 4) {
            g.drawString("1 ship", x + dx, y + dy + 8);
            dy += 14;
        }
    }
}
