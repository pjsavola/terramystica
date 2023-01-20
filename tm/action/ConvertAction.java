package tm.action;

import tm.Game;
import tm.Resources;

public class ConvertAction extends Action {

    private final Resources powerConversions;
    private final int priestsToWorkers;
    private final int workersToCoins;
    private final int pointsToCoins;

    public ConvertAction(Resources powerConversions, int priestsToWorkers, int workersToCoins, int pointsToCoins) {
        this.powerConversions = powerConversions;
        this.priestsToWorkers = priestsToWorkers;
        this.workersToCoins = workersToCoins;
        this.pointsToCoins = pointsToCoins;
    }

    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION;
    }

    @Override
    public boolean canExecute() {
        return powerConversions.power == 0 && player.canAffordPower(getPowerCost(powerConversions)) && player.canConvert(priestsToWorkers, workersToCoins, pointsToCoins);
    }

    @Override
    public void execute() {
        player.convert(priestsToWorkers, workersToCoins, pointsToCoins);
        player.convert(powerConversions);
    }

    @Override
    public boolean isFree() {
        return true;
    }

    @Override
    public String toString() {
        final int burn = player.getNeededBurn(getPowerCost(powerConversions));
        String result = burn > 0 ? ("Burn " + burn) : "";
        final int coins = powerConversions.coins;
        final int workers = powerConversions.workers;
        final int priests = powerConversions.priests;
        if (coins > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + coins + "pw to " + coins + "c";
        if (workers > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + (workers * 3) + "pw to " + workers + "w";
        if (priests > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + (priests * 5) + "pw to " + priests +"p";
        final int priestsToCoins = Math.min(priestsToWorkers, workersToCoins);
        final int priestsToWorkers = this.priestsToWorkers - priestsToCoins;
        final int workersToCoins = this.workersToCoins - priestsToCoins;
        if (priestsToCoins > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + priestsToCoins + "p to " + priestsToCoins + "c";
        if (priestsToWorkers > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + priestsToWorkers + "p to " + priestsToWorkers + "w";
        if (workersToCoins > 0) result += (result.isEmpty() ? "" : ". ") + "Convert " + workersToCoins + "w to " + workersToCoins + "c";
        if (pointsToCoins > 0) result += (result.isEmpty() ? "" : ". " ) + "Convert " + pointsToCoins + "vp to " + pointsToCoins + "c";
        return result;
    }

    public static int getPowerCost(Resources resources) {
        return resources.coins + 3 * (resources.workers) + 5 * resources.priests;
    }
}
