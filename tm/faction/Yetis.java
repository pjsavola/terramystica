package tm.faction;

import tm.Hex;
import tm.Resources;

public class Yetis extends Faction {

    public Yetis() {
        super("Yetis", Hex.Type.ICE);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 3, 0, 12);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 0, 0, 1, 1 };
    }

    @Override
    public Resources getDwellingIncome(int dwelling) {
        return Resources.w1;
    }

    @Override
    public Resources getTradingPostIncome(int tradingPost) {
        return Resources.c2pw2;
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
