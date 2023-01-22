package tm.action;

import tm.Cults;
import tm.Game;

public class CultStepAction extends Action {

    public enum Source { BON2, FAV6, ACTA, LEECH };

    private final int cult;
    private final int amount;
    private final Source source;

    public CultStepAction(int cult, int amount, Source source) {
        this.cult = cult;
        this.amount = amount;
        this.source = source;
    }

    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS;
    }

    public boolean canExecute() {
        return cult >= 0 && cult < 4;
    }

    public void execute() {
        final int[] steps = new int[4];
        steps[cult] = amount;
        player.addCultSteps(steps);
        switch (source) {
            case BON2 -> game.bonUsed[1] = true;
            case FAV6 -> player.usedFav6[0] = true;
            case ACTA -> player.usedFactionAction = true;
        }
    }

    @Override
    public String toString() {
        return "+" + Cults.getCultName(cult);
    }
}
