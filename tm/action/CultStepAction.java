package tm.action;

import tm.Cults;
import tm.Game;
import tm.Player;
import tm.faction.Acolytes;
import tm.faction.Auren;
import tm.faction.Cultists;

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
            case LEECH -> player.getFaction() instanceof Cultists && game.leechTrigger == player;
            case ACOLYTES -> player.getFaction() instanceof Acolytes && player.pendingCultSteps > 0;
            default -> true;
        };
    }

    private boolean isAmountValid() {
        if (source == Source.ACOLYTES) {
            return amount == 1 || amount == 2;
        }
        return amount == (source == Source.ACTA ? 2 : 1);
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS || (game.phase == Game.Phase.CONFIRM_ACTION && source == Source.ACOLYTES);
    }

    @Override
    public boolean canExecute() {
        return isSourceValid(source, game, player) && isAmountValid() && cult >= 0 && cult < 4;
    }

    @Override
    public void execute() {
        final int[] steps = new int[4];
        steps[cult] = amount;
        player.addCultSteps(steps);
        switch (source) {
            case BON2 -> game.bonUsed[1] = true;
            case FAV6 -> player.usedFav6[0] = true;
            case ACTA -> player.usedFactionAction = true;
            case ACOLYTES -> player.pendingCultSteps -= amount;
        }
    }

    @Override
    public boolean isFree() {
        return source == Source.ACOLYTES && !game.resolvingCultSpades();
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
