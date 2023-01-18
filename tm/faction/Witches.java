package tm.faction;

import tm.Hex;
import tm.Resources;

public class Witches extends Faction {

    public Witches() {
        super("Witches", Hex.Type.GREEN);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 0, 0, 0, 2 };
    }

    @Override
    public String getPowerAction(boolean stronghold) {
        return stronghold ? "free D" : null;
    }
}
