package tm.faction;

import tm.Hex;
import tm.Resources;

public class Cultists extends Faction {

    public Cultists() {
        super("Cultists", Hex.Type.BROWN);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 1, 0, 1, 0 };
    }

    @Override
    public Resources getStrongholdCost() {
        return Resources.c8w4;
    }

    @Override
    public Resources getSanctuaryCost() {
        return Resources.c8w4;
    }
}
