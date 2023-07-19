package tm.faction;

import tm.Hex;
import tm.Resources;

public class Auren extends Faction {

    public Auren() {
        super("Auren", Hex.Type.GREEN);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 0, 1, 0, 1 };
    }

    @Override
    public Resources getSanctuaryCost() {
        return Resources.c8w4;
    }

    @Override
    public String getPowerAction(boolean stronghold) {
        return stronghold ? "2 cult" : null;
    }
}
