package tm.faction;

import tm.Hex;
import tm.Resources;

public class IceMaidens extends Faction {

    public IceMaidens() {
        super("Ice Maidens", Hex.Type.ICE);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 0, 1, 0, 1 };
    }

    @Override
    public Resources getDwellingIncome(int dwelling) {
        return Resources.w1;
    }

    @Override
    public Resources getStrongholdIncome() {
        return Resources.pw4;
    }

    @Override
    public Resources getAdvanceDiggingCost() {
        return Resources.c5w1p1;
    }
}
