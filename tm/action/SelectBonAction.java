package tm.action;

import tm.Game;
import tm.Player;
import tm.faction.Faction;

public class SelectBonAction extends Action {

    private final Game game;
    private final int bonIndex;

    public SelectBonAction(Player player, Game game, int bonIndex) {
        super(player);
        this.game = game;
        this.bonIndex = bonIndex;
    }

    @Override
    public boolean canExecute() {
        return game.isValidBonIndex(bonIndex) && game.phase == Game.Phase.INITIAL_BONS || (game.phase == Game.Phase.ACTIONS && game.getRound() < 6);
    }

    @Override
    public void execute() {
        if (game.phase == Game.Phase.ACTIONS) {
            player.pass();
        }
        game.selectBon(player, bonIndex);
    }
}
