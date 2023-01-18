package tm.faction;

import tm.Hex;
import tm.Resources;

public class Swarmlings extends Faction {

    public Swarmlings() {
        super("Swarmlings", Hex.Type.BLUE);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(20, 8, 0, 9);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 1, 1, 1, 1 };
    }

    @Override
    public Resources getBaseIncome() {
        return Resources.w2;
    }

    @Override
    public Resources getDwellingCost() {
        return Resources.c3w2;
    }

    @Override
    public Resources getTradingPostCost() {
        return Resources.c4w3;
    }

    @Override
    public Resources getExpensiveTradingPostCost() {
        return Resources.c8w3;
    }

    @Override
    public Resources getTempleCost() {
        return Resources.c6w3;
    }

    @Override
    public Resources getStrongholdCost() {
        return Resources.c8w5;
    }

    @Override
    public Resources getSanctuaryCost() {
        return Resources.c8w5;
    }

    @Override
    public Resources getTradingPostIncome(int tradingPost) {
        return switch (tradingPost) {
            case 0 -> Resources.c2pw2;
            case 1 -> Resources.c2pw2;
            case 2 -> Resources.c2pw2;
            case 3 -> Resources.c3pw2;
            default -> throw new RuntimeException("Invalid trading post " + tradingPost);
        };
    }

    @Override
    public Resources getStrongholdIncome() {
        return Resources.pw4;
    }

    @Override
    public Resources getSanctuaryIncome() {
        return Resources.p2;
    }

    @Override
    public String getPowerAction(boolean stronghold) {
        return stronghold ? "free TP" : null;
    }
}
