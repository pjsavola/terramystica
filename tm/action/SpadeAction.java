package tm.action;

import tm.PowerActions;

public class SpadeAction extends Action {

    public enum Source { BON1 };

    private final int amount;
    private final Source source;

    public SpadeAction(int amount, Source source) {
        this.amount = amount;
        this.source = source;
    }

    @Override
    public boolean canExecute() {
        return amount >= 1 && amount <= 3;
    }

    @Override
    public void execute() {
        player.addSpades(amount);
        if (source == Source.BON1) {
            game.bonUsed[0] = true;
        }
    }

    @Override
    public String toString() {
        return "Action BON1";
    }
}
