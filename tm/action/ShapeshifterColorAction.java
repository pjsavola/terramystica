package tm.action;

import tm.Hex;
import tm.Resources;
import tm.faction.Shapeshifters;

public class ShapeshifterColorAction extends Action {

    private final boolean usePower;
    private final Hex.Type color;

    public ShapeshifterColorAction(boolean usePower, Hex.Type color) {
        this.usePower = usePower;
        this.color = color;
    }

    @Override
    public boolean canExecute() {
        if (game.getSelectedBaseOrdinals().contains(color.ordinal())) return false;
        if (usePower && !player.canAffordPower(5)) return false;
        if (!usePower && player.getPowerTokenCount() < 5) return false;

        return player.getFaction() instanceof Shapeshifters && player.hasStronghold();
    }

    @Override
    public void execute() {
        if (usePower) {
            player.pay(Resources.pw5);
        } else {
            player.payTokens(5);
        }
        game.setVariableColor(color);
    }

    @Override
    public String toString() {
        return usePower ? "Action ACTH5" : "Action ACTH6" + ". Pick-color " + color;
    }
}
