package tm.faction;

import tm.Hex;
import tm.Resources;

public class ChaosMagicians extends Faction {

    public ChaosMagicians() {
        super("Chaos Magicians", Hex.Type.RED);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 4, 0, 7);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 2, 0, 0, 0 };
    }

    @Override
    public Resources getStrongholdCost() {
        return Resources.c4w4;
    }

    @Override
    public Resources getSanctuaryCost() {
        return Resources.c8w4;
    }

    @Override
    public Resources getStrongholdIncome() {
        return Resources.w2;
    }

    @Override
    public String getPowerAction(boolean stronghold) {
        return stronghold ? "2x act" : null;
    }
}
