package tm.faction;

import tm.Hex;
import tm.Resources;

public class Riverwalkers extends Faction {

    public Riverwalkers() {
        super("Riverwalkers", Hex.Type.VARIABLE);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(10, 3, 0, 2);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 1, 0, 0, 1 };
    }

    @Override
    public Resources getDwellingIncome(int dwelling) {
        return dwelling == 2 || dwelling == 5 ? Resources.zero : Resources.w1;
    }

    @Override
    public Resources getTempleIncome(int temple) {
        return temple == 1 ? Resources.pw5 : Resources.p1;
    }

    @Override
    public int getMinDigging() {
        return 3;
    }

    public int getMaxShipping() {
        return 1;
    }
}
