package tm;

import tm.action.SelectFactionAction;
import tm.faction.Faction;

import javax.swing.*;
import java.awt.*;

public class FactionButton extends JButton {

    private final Faction faction;
    private final Hex hex;

    public FactionButton(JDialog dialog, Game game, Faction faction) {
        this.faction = faction;
        hex = new Hex("", faction.getHomeType());
        addActionListener(e -> {
            game.resolveAction(new SelectFactionAction(GameData.allFactions.indexOf(faction)));
            dialog.setVisible(false);
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        hex.draw((Graphics2D) g, 45, 45, 40);
        final Color oldColor = g.getColor();
        g.setColor(hex.getType().getFontColor());
        g.setFont(Hex.font);
        int y = 45;
        final FontMetrics metrics = g.getFontMetrics();
        final String[] name = faction.getName().split(" ");
        for (String txt : name) {
            final int w = metrics.stringWidth(txt);
            g.drawString(txt, 45 - w / 2, y);
            y += metrics.getHeight();
        }
        g.setColor(oldColor);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(90, 90);
    }
}
