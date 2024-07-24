package tm.faction;

import tm.Hex;
import tm.Resources;

public class Dragonlords extends Faction {

    public Dragonlords() {
        super("Dragonlords", Hex.Type.VOLCANO);
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
        return new int[] { 2, 0, 0, 0 };
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
