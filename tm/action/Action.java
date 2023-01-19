package tm.action;

import tm.Game;
import tm.Player;

public abstract class Action {
    protected Game game;
    protected Player player;

    public void setData(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS;
    }

    public abstract boolean canExecute();

    public abstract void execute();

    public boolean isPass() {
        return false;
    }

    public boolean needsConfirm() {
        return true;
    }

    public boolean isFree() {
        return false;
    }
}
