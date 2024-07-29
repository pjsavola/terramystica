package tm.faction;

import tm.Resources;

public class Shapeshifters extends Faction {

    public Shapeshifters() {
        super("Shapeshifters", null);
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
