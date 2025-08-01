package tm.action;

import tm.Game;
import tm.Player;
import tm.Resources;

public class AdvanceAction extends Action {

    private final boolean dig;
    private transient Resources powerConversions;
    private transient int burn;

    public AdvanceAction(boolean dig) {
        this.dig = dig;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        if (player.getFaction() != null) {
            final Resources cost = dig ? player.getFaction().getAdvanceDiggingCost() : player.getFaction().getAdvanceShippingCost();
            final Resources resources = player.getResources();
            final int coinsNeeded = Math.max(0, cost.coins - resources.coins);
            final int workersNeeded = Math.max(0, cost.workers - resources.workers);
            final int priestsNeeded = Math.max(0, cost.priests - resources.priests);
            if (coinsNeeded > 0 || workersNeeded > 0 || priestsNeeded > 0) {
                powerConversions = new Resources(coinsNeeded, workersNeeded, priestsNeeded, 0);
                final int powerNeeded = ConvertAction.getPowerCost(powerConversions);
                burn = player.getNeededBurn(powerNeeded);
            }
        }
    }

    @Override
    public boolean canExecute() {
        return dig ? player.canAdvanceDigging() : player.canAdvanceShipping();
    }

    @Override
    public void execute() {
        if (powerConversions != null) {
            if (burn > 0) {
                player.burn(burn);
            }
            player.convert(powerConversions);
        }
        if (dig) player.advanceDigging();
        else player.advanceShipping();
    }

    @Override
    public String toString() {
        String result = powerConversions == null ? "" : ConvertAction.getConversionString(burn, powerConversions);
        String advanceStr = dig ? "Advance dig" : "Advance ship";
        return result.isEmpty() ? advanceStr : result + ". " + advanceStr;
    }
}
