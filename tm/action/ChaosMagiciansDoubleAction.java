package tm.action;

import tm.faction.ChaosMagicians;
import tm.faction.Engineers;

public class ChaosMagiciansDoubleAction extends Action {

    @Override
    public boolean canExecute() {
        return player.getFaction() instanceof ChaosMagicians && player.hasStronghold();
    }

    @Override
    public void execute() {
        game.activateDoubleTurn();
        player.usedFactionAction = true;
    }

    @Override
    public boolean isFree() {
        return true;
    }

    @Override
    public String toString() {
        return "Action ACTC";
    }
}
