package tm;

import java.awt.*;

public abstract class Favs {

    public static final int[][] favCults = {{3, 0, 0, 0}, {0, 3, 0, 0}, {0, 0, 3, 0}, {0, 0, 0, 3}, {2, 0, 0, 0}, {0, 2, 0, 0}, {0, 0, 2, 0}, {0, 0, 0, 2}, {1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};

    public static Resources getFavIncome(int fav) {
        return switch (fav) {
            case 7 -> Resources.w2pw1;
            case 8 -> Resources.w4;
            case 9 -> Resources.c3;
            default -> Resources.zero;
        };
    }

    public static void drawFav(Graphics g, int x, int y, int fav, int count, boolean used) {
        g.setColor(new Color(0xEEDDDD));
        g.fillRect(x, y, 100, 100);

        g.setColor(Color.GRAY);
        g.drawLine(x + 5, y + 20, x + 95, y + 20);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, 100, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        final FontMetrics fontMetrics = g.getFontMetrics();
        String title = "FAV" + fav;
        if (count > 1) {
            title += " (x" + count + ")";
        }
        g.drawString(title, x + 3, y + 14);

        Graphics2D g2d = (Graphics2D) g;
        int dx = 3;
        int dy = 30;

        final int[] favCults = Favs.favCults[fav - 1];
        for (int cult = 0; cult < favCults.length; ++cult) {
            if (favCults[cult] > 0) {
                g.setColor(Cults.getCultColor(cult));
                final String txt = favCults[cult] + " " + Cults.getCultName(cult);
                g.fillRect(x + dx, y + dy - 4, fontMetrics.stringWidth(txt), 14);
                g.setColor(Color.BLACK);
                g.drawString(txt, x + dx, y + dy + 8);
                dy += 14;
                break;
            }
        }
        if (fav == 6) {
            final Color oldColor = g.getColor();
            final Stroke oldStroke = g2d.getStroke();
            PowerActions.drawPowerAction(g2d, x + dx, y + dy, "cult", used);
            g.setColor(oldColor);
            g2d.setStroke(oldStroke);
        }
        final Resources r = getFavIncome(fav);
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
        if (fav == 5) {
            g.drawString("-1 TOWN SIZE", x + dx, y + dy + 8);
            dy += 14;
        }
        else if (fav == 10) {
            g.drawString("TP >> 3", x + dx, y + dy + 8);
            dy += 14;
        }
        else if (fav == 11) {
            g.drawString("D >> 2", x + dx, y + dy + 8);
            dy += 14;
        }
        else if (fav == 12) {
            g.drawString("pass-vp:TP", x + dx, y + dy + 8);
            dy += 14;
            g.drawString("[0,2,3,3,4]", x + dx, y + dy + 8);
            dy += 14;
        }
    }
}
