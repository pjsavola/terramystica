package tm.action;

import tm.Game;
import tm.Player;

public class LeechAction extends Action {

    private final boolean accept;

    public LeechAction(boolean accept) {
        this.accept = accept;
    }

    public boolean validatePhase() {
        return game.phase == Game.Phase.LEECH;
    }

    public boolean canExecute() {
        return player.hasPendingLeech();
    }

    public void execute() {
        if (accept) {
            player.acceptLeech();
        }
    }

    public boolean needsConfirm() {
        return false;
    }

    @Override
    public void confirmed() {
        game.confirmLeech();
    }

    @Override
    public String toString() {
        return accept ? "Accept leech" : "Decline leech";
    }
}
