package tm.action;

import tm.Hex;
import tm.faction.Giants;

public class DigAction extends Action {

    private final Hex target;
    private final Hex.Type type;
    private final boolean jump;

    public DigAction(Hex target, Hex.Type type, boolean jump) {
        this.target = target;
        this.type = type;
        this.jump = jump;
    }

    public static int getSpadeCost(Hex hex, Hex.Type type) {
        final int delta = Math.abs((type.ordinal() - hex.getType().ordinal()));
        return Math.min(7 - delta, delta);
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

        final int spadeCount = player.getFaction() instanceof Giants ? 2 : getSpadeCost(target, type);
        return target != null && target.getStructure() == null && spadeCount != 0 && player.canDig(spadeCount, jump);
    }

    @Override
    public void execute() {
        final int spadeCount = player.getFaction() instanceof Giants ? 2 : getSpadeCost(target, type);
        if (jump) {
            player.useRange();
        }
        player.dig(spadeCount);
        player.useSpades(spadeCount);
        target.setType(type);
        if (type == player.getHomeType()) {
            player.addPendingBuild(target);
        }
    }

    @Override
    public String toString() {
        final int spadeCount = player.getFaction() instanceof Giants ? 2 : getSpadeCost(target, type);
        return "Dig " + spadeCount + ". Transform " + target.getId() + " to " + type;
    }
}
