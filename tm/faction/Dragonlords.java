package tm.faction;

import tm.Hex;

public class Dragonlords extends Faction {

    public Dragonlords() {
        super("Dragonlords", Hex.Type.VOLCANO);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 2, 0, 0, 0 };
    }
}
