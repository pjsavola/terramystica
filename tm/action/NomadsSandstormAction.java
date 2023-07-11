package tm.action;

import tm.faction.Nomads;

public class NomadsSandstormAction extends Action {

    @Override
    public boolean canExecute() {
        return player.getFaction() instanceof Nomads && player.hasStronghold() && !player.usedFactionAction;
    }

    @Override
    public void execute() {
        player.pendingSandstorm = true;
        player.usedFactionAction = true;
    }

    @Override
    public String toString() {
        return "Action ACTN";
    }
}
