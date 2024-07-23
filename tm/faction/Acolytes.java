package tm.faction;

import tm.Hex;

public class Acolytes extends Faction {

    public Acolytes() {
        super("Acolytes", Hex.Type.VOLCANO);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 3, 3, 3, 3 };
    }
}
