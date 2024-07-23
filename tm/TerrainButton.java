package tm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TerrainButton extends JButton {

    private final Hex hex;
    private final int spadeCost;

    public TerrainButton(JDialog dialog, String id, Hex.Type type, int spadeCost, Hex.Type[] result) {
        hex = new Hex(id, type);
        this.spadeCost = spadeCost;
        addActionListener(e -> {
            result[0] = hex.getType();
            dialog.setVisible(false);
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        final Color oldColor = g.getColor();
        hex.draw((Graphics2D) g, 40, 40, 40);
        g.setColor(hex.getType().getFontColor());
        g.setFont(Hex.font);
        final String txt = spadeCost == 0 ? "" : spadeCost + " spd";
        final FontMetrics metrics = g.getFontMetrics();
        final int w = metrics.stringWidth(txt);
        g.drawString(txt, 40 - w / 2, 40);
        g.setColor(oldColor);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(80, 80);
    }
}
