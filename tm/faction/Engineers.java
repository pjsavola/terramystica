package tm.faction;

import tm.Hex;
import tm.Resources;

public class Engineers extends Faction {

    public Engineers() {
        super("Engineers", Hex.Type.GRAY);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(10, 2, 0, 9);
    }

    @Override
    public Resources getBaseIncome() {
        return Resources.zero;
    }

    @Override
    public Resources getDwellingCost() {
        return Resources.c1w1;
    }

    @Override
    public Resources getTradingPostCost() {
        return Resources.c2w1;
    }

    @Override
    public Resources getExpensiveTradingPostCost() {
        return Resources.c4w1;
    }

    @Override
    public Resources getTempleCost() {
        return Resources.c4w1;
    }

    @Override
    public Resources getStrongholdCost() {
        return Resources.c6w3;
    }

    @Override
    public Resources getSanctuaryCost() {
        return Resources.c6w3;
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
    public Resources getStrongholdIncome() {
        return Resources.pw4;
    }

    @Override
    public String getPowerAction(boolean stronghold) {
        return "bridge";
    }
}
