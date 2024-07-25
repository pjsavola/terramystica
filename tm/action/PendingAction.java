package tm.action;

import tm.Game;

public abstract class PendingAction extends Action {

    @Override
    public final boolean validatePhase() {
        return game.phase == Game.Phase.CONFIRM_ACTION;
    }

    @Override
    public final boolean isPass() {
        return false;
    }

    @Override
    public final boolean isFree() {
        return false;
    }
}
