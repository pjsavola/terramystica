package tm;

import tm.Bons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class Pool extends JPanel {
    private final List<Integer> bons;
    private final int[] bonusCoins;
    private final List<Integer> favs;
    private final List<Integer> towns;

    public Pool(Game game, List<Integer> bons, int[] bonusCoins, List<Integer> favs, List<Integer> towns) {
        this.bons = bons;
        this.bonusCoins = bonusCoins;
        this.favs = favs;
        this.towns = towns;

        if (bonusCoins != null) {
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    final int px = e.getX();
                    final int py = e.getY();
                    final int col = px / 105;
                    final int row = py / 105;
                    if (10 * row + col < bons.size() && px % 105 >= 5 && py % 105 >= 5) {
                        game.bonClicked(10 * row + col);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
        }
    }

    @Override
    public void paint(Graphics g) {
        final boolean showCoins = bons.size() == 3 && bonusCoins != null;
        g.setFont(new Font("Arial", Font.BOLD, 16));
        int y = 4;
        int x = 5;
        if (bonusCoins != null) {
            g.drawString("Pool", x, y + 12);
            y += 16;
        }
        int items = 0;
        for (int i = 0; i < bons.size(); ++i) {
            Bons.drawBon(g, x, y, bons.get(i), showCoins ? bonusCoins[i] : 0);
            x += 105;
            if (++items == 10) {
                items = 0;
                x = 5;
                y += 105;
            }
        }
        for (int i = 0; i < favs.size(); ++i) {
            int fav = favs.get(i);
            int count = 1;
            while (i + 1 < favs.size() && favs.get(i + 1) == fav) {
                ++count;
                ++i;
            }
            Favs.drawFav(g, x, y, fav, count);
            x += 105;
            if (++items == 10) {
                items = 0;
                x = 5;
                y += 105;
            }
        }
        for (int i = 0; i < towns.size(); ++i) {
            int town = towns.get(i);
            int count = 1;
            while (i + 1 < towns.size() && towns.get(i + 1) == town) {
                ++count;
                ++i;
            }
            Towns.drawTown(g, x, y, town, count);
            x += 105;
            if (++items == 10) {
                items = 0;
                x = 5;
                y += 105;
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        final int itemCount = (int) (bons.size() + favs.stream().distinct().count() + towns.stream().distinct().count());
        return new Dimension(5 + 105 * bons.size(), 20 + 105 * ((itemCount - 1) / 10 + 1));
    }
}
