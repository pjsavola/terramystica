package tm.action;

import tm.Game;
import tm.Resources;

public class ConvertAction extends Action {

    private final Resources resources;

    public ConvertAction(Resources resources) {
        this.resources = resources;
    }

    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION;
    }

    @Override
    public boolean canExecute() {
        return resources.power == 0 && player.canAffordPower(getPowerCost(resources));
    }

    @Override
    public void execute() {
        player.convert(resources);
    }

    @Override
    public boolean isFree() {
        return true;
    }

    @Override
    public String toString() {
        final int burn = player.getNeededBurn(getPowerCost(resources));
        String result = burn > 0 ? ("Burn " + burn) : "";
        if (resources.coins > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + resources.coins + "pw to " + resources.coins + "c";
        if (resources.workers > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + resources.workers + "pw to " + resources.workers + "w";
        if (resources.priests > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + resources.priests + "pw to " + resources.priests +"p";
        return result;
    }

    public static int getPowerCost(Resources resources) {
       return  resources.coins + 3 * resources.workers + 5 * resources.priests;
    }
}
