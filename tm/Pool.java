package tm;

import tm.Bons;
import tm.action.CultStepAction;
import tm.action.SelectBonAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class Pool extends JPanel {
    private final Player player;
    private final List<Integer> bons;
    private final int[] bonusCoins;
    private final List<Integer> favs;
    private final List<Integer> towns;
    private final boolean[] bonUsed;
    private final boolean[] fav6Used;

    public Pool(Game game, Player player, List<Integer> bons, int[] bonusCoins, List<Integer> favs, List<Integer> towns, boolean[] bonUsed, boolean[] fav6Used) {
        this.player = player;
        this.bons = bons;
        this.bonusCoins = bonusCoins;
        this.favs = favs;
        this.towns = towns;
        this.bonUsed = bonUsed;
        this.fav6Used = fav6Used;

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (game.phase != Game.Phase.ACTIONS && game.phase != Game.Phase.INITIAL_BONS) return;

                final int px = e.getX();
                final int py = e.getY();
                final int col = px / 105;
                final int row = py / 105;
                final int idx = 10 * row + col;
                if (px % 105 >= 5 && py % 105 >= 5) {
                    if (idx < bons.size()) {
                        if (player != null && game.isMyTurn(player)) {
                            switch (bons.get(idx)) {
                                case 1 -> {
                                    // TODO
                                }
                                case 2 -> {
                                    if (PowerActions.actionClicked(px % 105 - 5 - 3, py % 105 - 5 - 30)) {
                                        final int cult = Cults.selectCult(game, 1, false);
                                        if (cult >= 0 && cult < 4) {
                                            game.resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.BON2));
                                        }
                                    }
                                }
                            }
                        } else if (player == null) {
                            game.resolveAction(new SelectBonAction(idx));
                        }
                    } else if (idx - bons.size() < favs.size()) {
                        if (player != null && game.isMyTurn(player)) {
                            if (favs.get(idx - bons.size()) == 6) {
                                if (PowerActions.actionClicked(px % 105 - 5 + 3, py % 105 - 5 + 30)) {
                                    final int cult = Cults.selectCult(game, 1, false);
                                    if (cult >= 0 && cult < 4) {
                                        game.resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.FAV6));
                                    }
                                }
                            }
                        }
                    }
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

    @Override
    public void paint(Graphics g) {
        final boolean showCoins = bons.size() == 3 && player == null;
        g.setFont(new Font("Arial", Font.BOLD, 16));
        int y = 4;
        int x = 5;
        if (player == null) {
            g.drawString("Pool", x, y + 12);
            y += 16;
        }
        int items = 0;
        for (int i = 0; i < bons.size(); ++i) {
            final int bon = bons.get(i);
            final boolean used = player != null && bon - 1 < bonUsed.length && bonUsed[bon - 1];
            Bons.drawBon(g, x, y, bon, showCoins ? bonusCoins[i] : 0, used);
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
            final boolean used = count == 1 && fav == 6 && fav6Used[0];
            Favs.drawFav(g, x, y, fav, count, used);
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
