package tm.action;

import tm.faction.Witches;

public class WitchesFreeDwellingAction extends Action {

    @Override
    public boolean canExecute() {
        return player.getFaction() instanceof Witches && player.hasStronghold() && !player.usedFactionAction;
    }

    @Override
    public void execute() {
        player.pendingFreeDwelling = true;
        player.usedFactionAction = true;
    }

    @Override
    public String toString() {
        return "Action ACTW";
    }
}
