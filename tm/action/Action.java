package tm.action;

import tm.Game;
import tm.Player;

public abstract class Action {
    protected Game game;
    protected Player player;

    public void setData(Game game, Player player) {
        if (this.game != null && this.game != game) {
            throw new RuntimeException("Game mismatch when rewinding");
        }
        if (this.player != null && this.player != player) {
            throw new RuntimeException("Player mismatch when rewinding");
        }
        this.game = game;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
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

    public void confirmed() {
    }

    public boolean isFree() {
        return false;
    }
}
