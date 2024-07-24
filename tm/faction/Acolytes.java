package tm.faction;

import tm.Hex;
import tm.Resources;

public class Acolytes extends Faction {

    public Acolytes() {
        super("Acolytes", Hex.Type.VOLCANO);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 3, 0, 6);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 3, 3, 3, 3 };
    }

    @Override
    public Resources getBaseIncome() {
        return Resources.zero;
    }

    @Override
    public Resources getStrongholdCost() {
        return Resources.c8w4;
    }

    @Override
    public Resources getSanctuaryCost() {
        return Resources.c8w4;
    }

    @Override
    public Resources getDwellingIncome(int dwelling) {
        return dwelling == 3 || dwelling == 7 ? Resources.zero : Resources.w1;
    }

    @Override
    public int getMinDigging() {
        return 3;
    }
}
