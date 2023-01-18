package tm.faction;

import tm.Hex;
import tm.Resources;

public class Halflings extends Faction {

    public Halflings() {
        super("Halflings", Hex.Type.BROWN);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 3, 0, 9);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 0, 0, 1, 1 };
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
    public Resources getAdvanceDiggingCost() {
        return Resources.c1w2p1;
    }
}
