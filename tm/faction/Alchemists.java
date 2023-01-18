package tm.faction;

import tm.Hex;
import tm.Resources;

public class Alchemists extends Faction {

    public Alchemists() {
        super("Alchemists", Hex.Type.BLACK);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 1, 1, 0, 0 };
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
    public Resources getStrongholdIncome() {
        return Resources.c6;
    }
}
