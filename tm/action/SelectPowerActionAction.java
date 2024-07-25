package tm.action;

import tm.PowerActions;

public class SelectPowerActionAction extends Action {

    private final int act;

    public SelectPowerActionAction(int act) {
        this.act = act;
    }

    @Override
    public boolean canExecute() {
        return act >= 1 && act <= 6 && !game.usedPowerActions[act - 1] && player.canAffordPower(PowerActions.getRequiredPower(act));
    }

    @Override
    public void execute() {
        player.usePowerAction(act);
    }

    @Override
    public String toString() {
        return "Action ACT" + act;
    }
}
