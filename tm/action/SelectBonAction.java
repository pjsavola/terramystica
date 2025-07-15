package tm.action;

import tm.Game;
import tm.Player;

public class SelectBonAction extends Action {

    private final int bonIndex;
    private transient String bon;

    public SelectBonAction(int bonIndex) {
        this.bonIndex = bonIndex;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        bon = Integer.toString(game.getBon(bonIndex));
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.INITIAL_BONS || game.phase == Game.Phase.ACTIONS;
    }

    @Override
    public boolean canExecute() {
        return game.isValidBonIndex(bonIndex) && game.getRound() < 6;
    }

    @Override
    public void execute() {
        if (game.phase == Game.Phase.ACTIONS) {
            player.pass();
        }
        game.selectBon(player, bonIndex);
    }

    @Override
    public boolean isPass() {
        return true;
    }

    @Override
    public boolean needsConfirm() {
        return game.phase == Game.Phase.ACTIONS;
    }

    @Override
    public String toString() {
        return "Pass BON" + bon;
    }
}
