package tm;

import tm.Bons;
import tm.action.*;

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
                final int px = e.getX();
                final int py = e.getY();
                final int col = px / 105;
                final int row = py / 105;
                final int idx = 10 * row + col;
                if (px % 105 >= 5 && py % 105 >= 5) {
                    final int bonCount = bons.size();
                    final int favCount = (int) favs.stream().distinct().count();
                    final int townCount = (int) towns.stream().distinct().count();
                    if (idx < bonCount) {
                        if (player != null && game.isMyTurn(player)) {
                            if (game.phase == Game.Phase.ACTIONS) {
                                if (PowerActions.actionClicked(px % 105 - 5 - 3, py % 105 - 5 - 30)) {
                                    switch (bons.get(idx)) {
                                        case 1 -> {
                                            game.resolveAction(new SpadeAction(SpadeAction.Source.BON1));
                                        }
                                        case 2 -> {
                                            if (CultStepAction.isSourceValid(CultStepAction.Source.BON2, game, player)) {
                                                final int cult = Cults.selectCult(game, 1, false);
                                                if (cult >= 0 && cult < 4) {
                                                    game.resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.BON2));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (player == null) {
                            if (game.phase == Game.Phase.INITIAL_BONS || game.phase == Game.Phase.ACTIONS) {
                                game.resolveAction(new SelectBonAction(idx));
                            }
                        }
                    } else if (idx - bonCount < favCount) {
                        if (player != null && game.isMyTurn(player)) {
                            if (game.phase == Game.Phase.ACTIONS) {
                                if (favs.get(idx - bons.size()) == 6) {
                                    if (PowerActions.actionClicked(px % 105 - 5 - 3, py % 105 - 5 - 44)) {
                                        if (CultStepAction.isSourceValid(CultStepAction.Source.FAV6, game, player)) {
                                            final int cult = Cults.selectCult(game, 1, false);
                                            if (cult >= 0 && cult < 4) {
                                                game.resolveAction(new CultStepAction(cult, 1, CultStepAction.Source.FAV6));
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (player == null) {
                            if (game.phase == Game.Phase.CONFIRM_ACTION) {
                                final int fav = favs.stream().distinct().toList().get(idx - bonCount);
                                game.resolveAction(new SelectFavAction(fav));
                            }
                        }
                    } else if (idx - bonCount - favCount < townCount) {
                        if (player == null) {
                            if (game.phase == Game.Phase.CONFIRM_ACTION) {
                                final int town = towns.stream().distinct().toList().get(idx - bonCount - favCount);
                                game.resolveAction(new SelectTownAction(town));
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
        final int itemsPerRow = player == null ? 10 : 6;
        int items = 0;
        for (int i = 0; i < bons.size(); ++i) {
            final int bon = bons.get(i);
            final boolean used = player != null && bon - 1 < bonUsed.length && bonUsed[bon - 1];
            Bons.drawBon(g, x, y, bon, showCoins ? bonusCoins[i] : 0, used);
            x += 105;
            if (++items == itemsPerRow) {
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
            final boolean used = count == 1 && fav == 6 && fav6Used != null && fav6Used[0];
            Favs.drawFav(g, x, y, fav, count, used);
            x += 105;
            if (++items == itemsPerRow) {
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
            if (++items == itemsPerRow) {
                items = 0;
                x = 5;
                y += 105;
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        final int itemsPerRow = player == null ? 10 : 6;
        final int itemCount = (int) (bons.size() + favs.stream().distinct().count() + towns.stream().distinct().count());
        final int cols = Math.min(itemCount, itemsPerRow);
        final int rows = (itemCount - 1) / itemsPerRow + 1;
        return new Dimension(5 + 105 * cols, 20 + 105 * rows);
    }
}
