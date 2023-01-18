package tm.faction;

import tm.Hex;
import tm.Resources;

public class Darklings extends Faction {

    public Darklings() {
        super("Darklings", Hex.Type.BLACK);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(15, 1, 1, 7);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 0, 1, 1, 0 };
    }

    @Override
    public Resources getSanctuaryCost() {
        return Resources.c10w4;
    }

    @Override
    public Resources getSanctuaryIncome() {
        return Resources.p2;
    }

    @Override
    public int getMinDigging() {
        return 3;
    }
}
