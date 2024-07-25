package tm.action;

import tm.Cults;
import tm.Game;
import tm.Player;
import tm.faction.Acolytes;
import tm.faction.Auren;

public class CultStepAction extends Action {

    public enum Source { BON2, FAV6, ACTA, LEECH, ACOLYTES };

    private final int cult;
    private final int amount;
    private final Source source;

    public CultStepAction(int cult, int amount, Source source) {
        this.cult = cult;
        this.amount = amount;
        this.source = source;
    }

    public static boolean isSourceValid(Source source, Game game, Player player) {
        return switch (source) {
            case BON2 -> player.getBon() == 2 && !game.bonUsed[1];
            case FAV6 -> player.hasFavor(6) && !player.usedFav6[0];
            case ACTA -> player.getFaction() instanceof Auren && !player.usedFactionAction && player.hasStronghold();
            case ACOLYTES -> player.getFaction() instanceof Acolytes && player.pendingCultSteps > 0;
            default -> true;
        };
    }

    private boolean isAmountValid() {
        return amount == (source == Source.ACTA ? 2 : 1);
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS || (game.phase == Game.Phase.CONFIRM_ACTION && source == Source.ACOLYTES);
    }

    public boolean canExecute() {
        return isSourceValid(source, game, player) && isAmountValid() && cult >= 0 && cult < 4;
    }

    public void execute() {
        final int[] steps = new int[4];
        steps[cult] = amount;
        player.addCultSteps(steps);
        switch (source) {
            case BON2 -> game.bonUsed[1] = true;
            case FAV6 -> player.usedFav6[0] = true;
            case ACTA -> player.usedFactionAction = true;
            case ACOLYTES -> --player.pendingCultSteps;
        }
    }

    public boolean isFree() {
        return source == Source.ACOLYTES;
    }

    @Override
    public String toString() {
        String act = switch (source) {
            case BON2 -> "Action BON2. ";
            case FAV6 -> "Action FAV6. ";
            case ACTA -> "Action ACTA. ";
            default -> "";
        };
        return act + "+" + (amount == 1 ? "" : amount) + Cults.getCultName(cult);
    }
}
