package tm;

import tm.action.*;
import tm.action.Action;
import tm.faction.Alchemists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Menus {

    public static void initializeMenus(Game game) {
        final JMenu actionMenu = new JMenu("Actions");
        final JMenu leechMenu = new JMenu("Leech");
        final JMenu convertMenu = new JMenu("Convert");
        final JMenu advanceMenu = new JMenu("Advance");
        game.menus = new JMenu[] { actionMenu, leechMenu, convertMenu, advanceMenu };

        // === ACTION MENU ===
        new ActionMenuItem(game, actionMenu, "Confirm turn", KeyEvent.VK_ENTER) {
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.CONFIRM_ACTION && (game.getCurrentPlayer().getPendingActions().isEmpty() || !game.getCurrentPlayer().getSkippablePendingActions().isEmpty());
            }

            @Override
            protected void execute(Game game) {
                final Set<Player.PendingType> skippableActions = game.getCurrentPlayer().getSkippablePendingActions();
                int option = JOptionPane.OK_OPTION;
                if (!skippableActions.isEmpty()) {
                    final String pending = "Are you sure you want to skip: " + skippableActions.stream().map(Player.PendingType::getDescription).collect(Collectors.joining(" / ")) + "?";
                    option = JOptionPane.showConfirmDialog(null, pending, "Skip Action?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                }
                if (option == JOptionPane.OK_OPTION) {
                    game.confirmTurn();
                }
            }
        };
        new ActionMenuItem(game, actionMenu, "Undo", KeyEvent.VK_ESCAPE) {
            public boolean canExecute(Game game) {
                return game.canRewind();
            }

            @Override
            protected void execute(Game game) {
                game.rewind();
            }
        };
        new ActionMenuItem(game, actionMenu, "Final Pass") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS && game.getRound() == 6;
            }

            @Override
            protected void execute(Game game) {
                game.resolveAction(new PassAction());
            }
        };
        new ActionMenuItem(game, actionMenu, "Darklings SH Conversion") {
            @Override
            public boolean canExecute(Game game) {
                return game.getCurrentPlayer().getPendingActions().contains(Player.PendingType.CONVERT_W2P);
            }

            @Override
            protected void execute(Game game) {
                final int workers = game.getCurrentPlayer().getWorkers();
                final String[] choices = IntStream.range(0, Math.min(3, workers) + 1).boxed().map(Object::toString).toArray(String[]::new);
                final int response = JOptionPane.showOptionDialog(game, "Convert W to P...", "Darklings SH Conversion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, null);
                if (response >= 0 && response < choices.length) {
                    game.resolveAction(new DarklingsConvertAction(response));
                }
            }
        };

        // === LEECH MENU ===
        new ActionMenuItem(game, leechMenu, "Accept", KeyEvent.VK_ENTER) {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.LEECH && game.getCurrentPlayer().getPendingLeech() > 0;
            }

            @Override
            protected void execute(Game game) {
                game.resolveAction(new LeechAction(true));
            }
        };
        new ActionMenuItem(game, leechMenu, "Decline", KeyEvent.VK_ESCAPE) {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.LEECH && game.getCurrentPlayer().getPendingLeech() > 0;
            }

            @Override
            protected void execute(Game game) {
                game.resolveAction(new LeechAction(false));
            }
        };

        // === CONVERT MENU ===
        new ActionMenuItem(game, convertMenu, "Convert ...") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION;
            }

            @Override
            protected void execute(Game game) {
                final boolean alchemists = game.getCurrentPlayer().getFaction() instanceof Alchemists;
                final JTextField priestsToWorkers = new JTextField();
                final JTextField workersToCoins = new JTextField();
                final JTextField powerToPriests = new JTextField();
                final JTextField powerToWorkers = new JTextField();
                final JTextField powerToCoins = new JTextField();
                final JTextField coinsToPoints = new JTextField();
                final JTextField pointsToCoins = new JTextField();
                Object[] message = {
                        "P -> W:", priestsToWorkers,
                        "W -> C:", workersToCoins,
                        "5 PW -> P", powerToPriests,
                        "3 PW -> W", powerToWorkers,
                        "1 PW -> C", powerToCoins,
                        (alchemists ? "2" : "3") + " C -> VP", coinsToPoints,
                        "1 VP -> C", pointsToCoins
                };
                message = Arrays.stream(message).limit(alchemists ? 14 : 12).toArray();

                int option = JOptionPane.showConfirmDialog(null, message, "Convert ...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        final int p2w = priestsToWorkers.getText().isEmpty() ? 0 : Integer.parseInt(priestsToWorkers.getText());
                        final int w2c = workersToCoins.getText().isEmpty() ? 0 : Integer.parseInt(workersToCoins.getText());
                        final int vp2c = pointsToCoins.getText().isEmpty() ? 0 : Integer.parseInt(pointsToCoins.getText());
                        final int pw2p = powerToPriests.getText().isEmpty() ? 0 : Integer.parseInt(powerToPriests.getText());
                        final int pw2w = powerToWorkers.getText().isEmpty() ? 0 : Integer.parseInt(powerToWorkers.getText());
                        final int pw2c = powerToCoins.getText().isEmpty() ? 0 : Integer.parseInt(powerToCoins.getText());
                        final int c2vp = coinsToPoints.getText().isEmpty() ? 0 : Integer.parseInt(coinsToPoints.getText());
                        Resources powerConversions = Resources.zero;
                        if (pw2p > 0) powerConversions = powerConversions.combine(Resources.fromPriests(pw2p));
                        if (pw2w > 0) powerConversions = powerConversions.combine(Resources.fromWorkers(pw2w));
                        if (pw2c > 0) powerConversions = powerConversions.combine(Resources.fromCoins(pw2c));
                        if (powerConversions != Resources.zero || p2w > 0 || w2c > 0 || vp2c > 0 || c2vp > 0) {
                            game.resolveAction(new ConvertAction(powerConversions, p2w, w2c, vp2c, c2vp));
                        }
                    } catch (NumberFormatException ex) {
                        final String input = ex.getMessage().substring(ex.getMessage().indexOf('"'));
                        JOptionPane.showConfirmDialog(null, "Invalid number: " + input, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null);
                    }
                }
            }
        };
        new ConvertMenuItem(game, convertMenu, "PW -> C", KeyEvent.VK_C, Resources.c1);
        new ConvertMenuItem(game, convertMenu, "3PW -> W", KeyEvent.VK_W, Resources.w1);
        new ConvertMenuItem(game, convertMenu, "5PW -> P", KeyEvent.VK_P, Resources.p1);
        new ActionMenuItem(game, convertMenu, "VP -> C", KeyEvent.VK_A) {
            @Override
            public boolean canExecute(Game game) {
                return (game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION) && game.getCurrentPlayer().canConvert(0, 0, 1, 0);
            }

            @Override
            protected void execute(Game game) {
                game.resolveAction(new ConvertAction(Resources.zero, 0, 0, 1, 0));
            }
        };
        new ActionMenuItem(game, convertMenu, "Burn ...") {
            @Override
            public boolean canExecute(Game game) {
                return (game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION) && game.getCurrentPlayer().getMaxBurn() > 0;
            }

            @Override
            protected void execute(Game game) {
                final JTextField burnField = new JTextField();
                final Object[] message = { "Burn:", burnField };
                int option = JOptionPane.showConfirmDialog(null, message, "Burn ...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        final int burn = burnField.getText().isEmpty() ? 0 : Integer.parseInt(burnField.getText());
                        if (burn > 0) {
                            game.resolveAction(new BurnAction(burn));
                        }
                    } catch (NumberFormatException ex) {
                        final String input = ex.getMessage().substring(ex.getMessage().indexOf('"'));
                        JOptionPane.showConfirmDialog(null, "Invalid number: " + input, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null);
                    }
                }
            }
        };

        //=== ADVANCE MENU ===
        new ActionMenuItem(game, advanceMenu,"Advance ship") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS && game.getCurrentPlayer().canAdvanceShipping();
            }

            @Override
            protected void execute(Game game) {
                game.resolveAction(new AdvanceAction(false));
            }
        };
        new ActionMenuItem(game, advanceMenu, "Advance dig") {
            @Override
            public boolean canExecute(Game game) {
                return game.phase == Game.Phase.ACTIONS && game.getCurrentPlayer().canAdvanceDigging();
            }

            @Override
            protected void execute(Game game) {
                game.resolveAction(new AdvanceAction(true));
            }
        };
    }
}
