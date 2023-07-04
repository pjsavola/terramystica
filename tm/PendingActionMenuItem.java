package tm;

import java.awt.*;

public abstract class PendingActionMenuItem extends ActionMenuItem {
    @Override
    public Game.Phase getPhase() {
        return Game.Phase.CONFIRM_ACTION;
    }
    public abstract Player.PendingType getPendingType();
}
