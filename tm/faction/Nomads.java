package tm.faction;

import tm.Hex;
import tm.Resources;

public class Nomads extends Faction {

    public Nomads() {
        super("Nomads", Hex.Type.YELLOW);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 2, 0, 7);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 1, 0, 1, 0 };
    }

    @Override
    public Resources getTradingPostIncome(int tradingPost) {
        return switch (tradingPost) {
            case 0 -> Resources.c2pw1;
            case 1 -> Resources.c2pw1;
            case 2 -> Resources.c3pw1;
            case 3 -> Resources.c4pw1;
            default -> throw new RuntimeException("Invalid trading post " + tradingPost);
        };
    }

    @Override
    public String getPowerAction(boolean stronghold) {
        return stronghold ? "transform" : null;
    }
}
