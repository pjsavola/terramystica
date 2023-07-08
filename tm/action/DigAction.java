package tm.action;

import tm.Game;
import tm.Hex;
import tm.Player;
import tm.faction.Giants;

public class DigAction extends Action {

    private final Hex target;
    private final Hex.Type type;
    private final boolean jump;
    private int requiredSpades;
    private int requiredDigging;

    public DigAction(Hex target, Hex.Type type, boolean jump) {
        this.target = target;
        this.type = type;
        this.jump = jump;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        requiredSpades = player.getFaction() instanceof Giants ? 2 : getSpadeCost(target, type);
        requiredDigging = Math.max(0, requiredSpades - player.getPendingSpades());
    }

    public static int getSpadeCost(Hex hex, Hex.Type type) {
        final int delta = Math.abs((type.ordinal() - hex.getType().ordinal()));
        return Math.min(7 - delta, delta);
    }

    @Override
    public final boolean validatePhase() {
        if (game.phase == Game.Phase.CONFIRM_ACTION) {
            return player.getPendingActions().contains(Player.PendingType.USE_SPADES);
        }
        return super.validatePhase();
    }

    @Override
    public boolean canExecute() {
        final boolean reachable = game.isReachable(target, player);
        if (jump) {
            if (reachable || !game.isJumpable(target, player)) {
                return false;
            }
        } else if (!reachable) {
            return false;
        }

        // Partial usage of pending spades to anything but home terrain is not allowed.
        if (player.getPendingSpades() > 1 && type != player.getHomeType() && requiredSpades < 1) return false;
        return target.getStructure() == null && requiredSpades != 0 && player.canDig(requiredDigging, jump);
    }

    @Override
    public void execute() {
        if (jump) {
            player.useRange(true);
        }
        if (requiredDigging > 0) {
            player.dig(requiredDigging);
        }
        player.useSpades(requiredSpades);
        target.setType(type);
        if (type == player.getHomeType()) {
            player.addPendingBuild(target);
        }
    }

    @Override
    public String toString() {
        final String txt = "Transform " + target.getId() + " to " + type;
        if (requiredDigging > 0) {
            return "Dig " + requiredDigging + ". " + txt;
        }
        return txt;
    }
}
