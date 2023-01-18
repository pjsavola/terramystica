package tm.faction;

import tm.Hex;
import tm.Resources;

public class Mermaids extends Faction {

    public Mermaids() {
        super("Mermaids", Hex.Type.BLUE);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 3, 0, 9);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 0, 2, 0, 0 };
    }

    @Override
    public Resources getSanctuaryCost() {
        return Resources.c8w4;
    }

    @Override
    public Resources getStrongholdIncome() {
        return Resources.pw4;
    }

    @Override
    public int getMaxShipping() {
        return 5;
    }

    @Override
    public int getAdvanceShippingPoints(int level) {
        return switch (level) {
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> 5;
            default -> throw new RuntimeException("Invalid shipping level " + level);
        };
    }
}
