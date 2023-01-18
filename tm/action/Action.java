package tm.action;

import tm.Player;

public abstract class Action {
    protected final Player player;

    protected Action(Player player) {
        this.player = player;
    }

    public abstract boolean canExecute();
    public abstract void execute();
}
