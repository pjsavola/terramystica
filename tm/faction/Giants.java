package tm.faction;

import tm.Hex;
import tm.Resources;

public class Giants extends Faction {

    public Giants() {
        super("Giants", Hex.Type.RED);
    }

    @Override
    public int[] getInitialCultSteps() {
        return new int[] { 1, 0, 0, 1 };
    }

    @Override
    public Resources getStrongholdIncome() {
        return Resources.pw4;
    }

    @Override
    public String getPowerAction(boolean stronghold) {
        return stronghold ? "2 spd" : null;
    }
}
