package tm.faction;

import tm.Hex;
import tm.Resources;

public class Shapeshifters extends Faction {

    public Shapeshifters() {
        super("Shapeshifters", Hex.Type.VARIABLE);
    }

    @Override
    public int getInitialPowerTokenCount() {
        return 8;
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 3, 0, 4);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 1, 1, 0, 0 };
    }

    @Override
    public Resources getStrongholdCost() {
        return Resources.c6w3;
    }

    @Override
    public Resources getStrongholdIncome() {
        return Resources.pw4;
    }

    @Override
    public int getMinDigging() {
        return 3;
    }

    @Override
    public String getPowerAction(boolean stronghold) {
        return stronghold ? "color" : null;
    }
}
