package tm.faction;

import tm.Hex;
import tm.Resources;

public class Fakirs extends Faction {

    public Fakirs() {
        super("Fakirs", Hex.Type.YELLOW);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 3, 0, 5);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 1, 0, 0, 1 };
    }

    @Override
    public Resources getStrongholdCost() {
        return Resources.c10w4;
    }

    @Override
    public Resources getStrongholdIncome() {
        return Resources.p1;
    }

    @Override
    public int getMinDigging() {
        return 2;
    }

    @Override
    public int getMaxShipping() {
        return 0;
    }
}
