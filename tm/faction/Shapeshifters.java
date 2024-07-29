package tm.faction;

import tm.Hex;
import tm.Resources;

public class Shapeshifters extends Faction {

    public Shapeshifters() {
        super("Shapeshifters", Hex.Type.VARIABLE);
    }

    @Override
    public Resources getInitialIncome() {
        return new Resources(10, 3, 0, 2);
    }

    @Override
    public Resources getStrongholdCost() {
        return Resources.c6w3;
    }

}
