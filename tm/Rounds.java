package tm;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Rounds extends JPanel {

    private final List<Round> rounds;
    private final static Font font = new Font("Arial", Font.PLAIN, 12);
    int round;

    public Rounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    @Override
    public void paint(Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;

        final Stroke oldStroke = g2d.getStroke();
        final Color oldColor = g.getColor();

        final FontMetrics metrics = g.getFontMetrics();
        final int h = metrics.getHeight();

        int x = 12;
        int y = 2;
        for (int i = 0; i < rounds.size(); ++i) {
            final Round round = rounds.get(i);
            final boolean lastRound = i == rounds.size() - 1;
            if (i + 1 == this.round) {
                g.setColor(new Color(0x55FF99));
                g.fillRect(x, y, 100, 100);
            }
            g.setColor(Color.BLACK);
            g.drawRect(x, y, 100, 100);

            final String cultCondition = cultConditionToString(round) + " ->";
            final String cultIncome = cultRewardToString(round);
            Color cultColor = getCultColor(round);
            if (cultColor != null && !lastRound) {
                if (i + 1 < this.round) {
                    cultColor = new Color(cultColor.getRed(), cultColor.getGreen(), cultColor.getBlue(), 50);
                }
                g.setColor(cultColor);
                final int w1 = metrics.stringWidth(cultCondition);
                g.fillRect(x + 2, y + 56 - h + 2, w1, h);

                final int w2 = metrics.stringWidth(cultIncome);
                g.fillRect(x + 2, y + 72 - h + 2, w2, h);
            }

            if (i + 1 < this.round) {
                g.setColor(Color.LIGHT_GRAY);
            } else {
                g.setColor(Color.BLACK);
            }

            final String cultScoring = scoringToString(round);
            g.drawString(cultScoring, x + 2, y + 20);
            if (!lastRound) {
                g.drawString(cultCondition, x + 2, y + 56);
                g.drawString(cultIncome, x + 2, y + 72);
            }

            x += 110;
        }

        g.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(664, 104);
    }

    private String scoringToString(Round round) {
        if (round.d > 0) return "D >> " + round.d;
        if (round.tp > 0) return "TP >> " + round.tp;
        if (round.te > 0) return "TE >> " + round.te;
        if (round.shsa > 0) return "SH/SA >> " + round.shsa;
        if (round.spade > 0) return "SPADE >> " + round.spade;
        if (round.town > 0) return "TOWN >> " + round.town;
        return "";
    }

    private Color getCultColor(Round round) {
        if (round.fire > 0) return Cults.getCultColor(0);
        if (round.water > 0) return Cults.getCultColor(1);
        if (round.earth > 0) return Cults.getCultColor(2);
        if (round.air > 0) return Cults.getCultColor(3);
        return null;
    }

    private String cultConditionToString(Round round) {
        if (round.fire > 0) return round.fire + " FIRE";
        if (round.water > 0) return round.water + " WATER";
        if (round.earth > 0) return round.earth + " EARTH";
        if (round.air > 0) return round.air + " AIR";
        if (round.priests > 0) return round.priests + " CULT_P";
        return "";
    }

    private String cultRewardToString(Round round) {
        if (round.income.coins > 0) return round.income.coins + " C";
        if (round.income.workers > 0) return round.income.workers + " W";
        if (round.income.priests > 0) return round.income.priests + " P";
        if (round.income.power > 0) return round.income.power + " PW";
        if (round.income == Resources.spade) return "1 SPADE";
        return "";
    }
}
