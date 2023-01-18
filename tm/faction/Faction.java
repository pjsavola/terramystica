package tm.faction;

import tm.Hex;
import tm.Resources;

public abstract class Faction {
    private final String name;
    private final Hex.Type homeType;

    protected Faction(String name, Hex.Type homeType) {
        this.name = name;
        this.homeType = homeType;
    }

    public String getName() {
        return name;
    }

    public Hex.Type getHomeType() {
        return homeType;
    }

    public Resources getInitialIncome() {
        return new Resources(15, 3, 0, 7);
    }

    public int[] getInitialCultSteps() {
        return new int[] { 0, 0, 0, 0 };
    }

    public Resources getBaseIncome() {
        return Resources.w1;
    }

    public Resources getDwellingIncome(int dwelling) {
        return dwelling == 8 ? Resources.zero : Resources.w1;
    }

    public Resources getTradingPostIncome(int tradingPost) {
        return tradingPost < 2 ? Resources.c2pw1 : Resources.c2pw2;
    }

    public Resources getTempleIncome(int temple) {
        return Resources.p1;
    }

    public Resources getStrongholdIncome() {
        return Resources.pw2;
    }

    public Resources getSanctuaryIncome() {
        return Resources.p1;
    }

    public Resources getDwellingCost() {
        return Resources.c2w1;
    }

    public Resources getTradingPostCost() {
        return Resources.c3w2;
    }

    public Resources getExpensiveTradingPostCost() {
        return Resources.c6w2;
    }

    public Resources getTempleCost() {
        return Resources.c5w2;
    }

    public Resources getStrongholdCost() {
        return Resources.c6w4;
    }

    public Resources getSanctuaryCost() {
        return Resources.c6w4;
    }

    public int getMinDigging() {
        return 1;
    }

    public int getMaxShipping() {
        return 3;
    }

    public int getAdvanceShippingPoints(int level) {
        return switch (level) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            default -> throw new RuntimeException("Invalid shipping level");
        };
    }

    public Resources getAdvanceDiggingCost() {
        return Resources.c6w2p1;
    }

    public Resources getAdvanceShippingCost() {
        return Resources.c4p1;
    }

    public String getPowerAction(boolean stronghold) {
        return null;
    }
}
