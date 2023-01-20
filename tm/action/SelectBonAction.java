package tm.action;

import tm.Game;
import tm.Player;
import tm.faction.Faction;

public class SelectBonAction extends Action {

    private final int bonIndex;

    public SelectBonAction(int bonIndex) {
        this.bonIndex = bonIndex;
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
        return "Pass BON" + game.getBon(bonIndex);
    }
}
