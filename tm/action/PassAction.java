package tm.action;

import tm.Game;
import tm.Player;

public class PassAction extends Action {

    private final Game game;

    public PassAction(Player player, Game game) {
        super(player);
        this.game = game;
    }

    @Override
    public boolean canExecute() {
        return game.phase == Game.Phase.ACTIONS && game.getRound() == 6;
    }

    @Override
    public void execute() {
        player.pass();
    }
}
