package tm.action;

import tm.faction.Giants;

public class SpadeAction extends Action {

    public enum Source { BON1, ACTG };

    private final Source source;

    public SpadeAction(Source source) {
        this.source = source;
    }

    @Override
    public boolean canExecute() {
        return switch (source) {
            case BON1 -> player.getBon() == 1 && !game.bonUsed[0];
            case ACTG -> player.getFaction() instanceof Giants && player.hasStronghold() && !player.usedFactionAction;
        };
    }

    @Override
    public void execute() {
        switch (source) {
            case BON1 -> {
                player.addSpades(1, true);
                game.bonUsed[0] = true;
            }
            case ACTG -> {
                player.addSpades(2, true);
                player.usedFactionAction = true;
            }
        }
    }

    @Override
    public String toString() {
        return switch (source) {
            case BON1 -> "Action BON1";
            case ACTG -> "Action ACTG";
        };
    }
}
