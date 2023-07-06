package tm.action;

import tm.PowerActions;

public class SpadeAction extends Action {

    public enum Source { BON1, DIGGING };

    private final int amount;
    private final Source source;

    public SpadeAction(int amount, Source source) {
        this.amount = amount;
        this.source = source;
    }

    @Override
    public boolean canExecute() {
        return switch (source) {
            case BON1 -> !game.bonUsed[0] && amount == 1;
            case DIGGING -> player.canDig(amount) && amount > 0 && amount <= 3;
        };
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
        return switch (source) {
            case BON1 -> "Action BON1";
            case DIGGING -> "Dig " + amount;
        };
    }
}
