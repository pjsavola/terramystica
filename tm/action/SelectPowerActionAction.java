package tm.action;

import tm.PowerActions;
import tm.faction.Yetis;

public class SelectPowerActionAction extends Action {

    private final int act;

    public SelectPowerActionAction(int act) {
        this.act = act;
    }

    @Override
    public boolean canExecute() {
        if (act < 1 || act > 6) return false;

        return (!game.usedPowerActions[act - 1] || (player.getFaction() instanceof Yetis && player.hasStronghold())) && player.canAffordPower(PowerActions.getRequiredPower(player, act));
    }

    @Override
    public void execute() {
        player.usePowerAction(act);
    }

    @Override
    public String toString() {
        final int burn = player.getNeededBurn(PowerActions.getRequiredPower(player, act));
        final String burnStr = burn > 0 ? ("Burn " + burn + ". ") : "";
        return burnStr + "Action ACT" + act;
    }
}
