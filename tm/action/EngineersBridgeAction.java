package tm.action;

import tm.faction.Engineers;

public class EngineersBridgeAction extends Action {

    @Override
    public boolean canExecute() {
        return player.getFaction() instanceof Engineers && player.getWorkers() >= 2 && player.getBridgesLeft() > 0 && player.getPendingActions().isEmpty();
    }

    @Override
    public void execute() {
        player.getEngineerBridge();
    }

    @Override
    public String toString() {
        return "Action ACTE";
    }
}
