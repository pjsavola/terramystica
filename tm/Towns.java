package tm;

import java.awt.*;

public abstract class Towns {

    public static Resources getTownIncome(int town) {
        return switch (town) {
            case 1 -> Resources.c6;
            case 2 -> Resources.w2;
            case 3 -> Resources.p1;
            case 4 -> Resources.pw8;
            default -> Resources.zero;
        };
    }

    public static int getTownPoints(int town) {
        return switch (town) {
            case 1 -> 5;
            case 2 -> 7;
            case 3 -> 9;
            case 4 -> 6;
            case 5 -> 8;
            case 6 -> 2;
            case 7 -> 4;
            case 8 -> 11;
            default -> throw new RuntimeException("Invalid town " + town);
        };
    }

    public static void drawTown(Graphics g, int x, int y, int town, int count) {
        g.setColor(new Color(0xDDEEFF));
        g.fillRect(x, y, 100, 100);

        g.setColor(Color.GRAY);
        g.drawLine(x + 5, y + 20, x + 95, y + 20);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, 100, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        final FontMetrics fontMetrics = g.getFontMetrics();
        String title = "TW" + town;
        if (count > 1) {
            title += " (x" + count + ")";
        }
        g.drawString(title, x + 3, y + 14);

        int dx = 3;
        int dy = 30;

        final int points = getTownPoints(town);
        if (points > 0) {
            String txt = points + " vp";
            if (town == 6) {
                txt += ", 2 keys";
            }
            g.drawString(txt, x + dx, y + dy + 8);
            dy += 14;
        }
        if (town == 5 || town == 6) {
            for (int cult = 0; cult < 4; ++cult) {
                g.setColor(Cults.getCultColor(cult));
                final String txt = (town == 5 ? 1 : 2) + " " + Cults.getCultName(cult);
                g.fillRect(x + dx, y + dy - 4, fontMetrics.stringWidth(txt), 14);
                g.setColor(Color.BLACK);
                g.drawString(txt, x + dx, y + dy + 8);
                dy += 14;
            }
        }
        final Resources r = getTownIncome(town);
        if (r.coins > 0) {
            g.drawString(r.coins + " C", x + dx, y + dy + 8);
            dy += 14;
        }
        if (r.workers > 0) {
            g.drawString(r.workers + " W", x + dx, y + dy + 8);
            dy += 14;
        }
        if (r.priests > 0) {
            g.drawString(r.priests + " P", x + dx, y + dy + 8);
            dy += 14;
        }
        if (r.power > 0) {
            g.drawString(r.power + " PW", x + dx, y + dy + 8);
            dy += 14;
        }
        if (town == 7) {
            g.drawString(r.power + "1 carpet range", x + dx, y + dy + 8);
            dy += 14;
            g.drawString(r.power + "1 shipping", x + dx, y + dy + 8);
            dy += 14;
        }
    }
}
