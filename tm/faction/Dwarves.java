package tm.faction;

import tm.Hex;
import tm.Resources;

public class Dwarves extends Faction {

    public Dwarves() {
        super("Dwarves", Hex.Type.GRAY);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 0, 0, 2, 0 };
    }

    @Override
    public Resources getTradingPostIncome(int tradingPost) {
        return switch (tradingPost) {
            case 0 -> Resources.c3pw1;
            case 1 -> Resources.c2pw1;
            case 2 -> Resources.c2pw2;
            case 3 -> Resources.c3pw2;
            default -> throw new RuntimeException("Invalid trading post " + tradingPost);
        };
    }

    @Override
    public int getMaxShipping() {
        return 0;
    }
}
