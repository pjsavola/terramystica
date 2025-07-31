package tm.action;

import tm.*;
import tm.faction.Darklings;
import tm.faction.Giants;

public class DigAction extends Action {

    private final int row;
    private final int col;
    private final Hex.Type type;
    private final boolean jump;
    private final int cult;
    private transient Hex target;
    private transient int requiredSpades;
    private transient int requiredDigging;
    private transient int pendingSpades;
    private transient boolean resolvingCultSpades;
    private transient Resources powerConversions;
    private transient int burn;

    public DigAction(int row, int col, Hex.Type type, boolean jump) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.jump = jump;
        cult = -1;
    }

    public DigAction(int row, int col, Hex.Type type, int cult) {
        this.row = row;
        this.col = col;
        this.type = type;
        jump = false;
        this.cult = cult;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        target = game.getHex(row, col);
        if (player.getFaction() != null) {
            if (player.getFaction().getHomeType() == Hex.Type.VOLCANO) {
                requiredSpades = 1;
                requiredDigging = game.getVolcanoDigCost(target, player);
            } else {
                final Hex.Type effectiveType = type == Hex.Type.ICE ? game.getIceColor() : type;
                requiredSpades = Math.max(1, player.getFaction() instanceof Giants ? 2 : getSpadeCost(target, effectiveType));
                pendingSpades = player.getPendingSpades();
                requiredDigging = Math.max(0, requiredSpades - pendingSpades);
                resolvingCultSpades = game.resolvingCultSpades();
                if (!resolvingCultSpades && (requiredDigging > 0 || jump)) {
                    final Resources resources = player.getResources();
                    if (player.getFaction() instanceof Darklings) {
                        final int priestsNeeded = Math.max(0, requiredDigging - resources.priests);
                        if (priestsNeeded > 0) {
                            powerConversions = Resources.fromPriests(priestsNeeded);
                        }
                    } else {
                        final Resources digCost = Resources.fromWorkers(requiredDigging * player.getDigging());
                        final Resources cost = jump ? player.getJumpCost().combine(digCost) : digCost;
                        final int priestsNeeded = Math.max(0, cost.priests - resources.priests);
                        final int workersNeeded = Math.max(0, cost.workers - resources.workers);
                        if (priestsNeeded > 0 || workersNeeded > 0) {
                            powerConversions = new Resources(0, workersNeeded, priestsNeeded, 0);
                        }
                    }
                    if (powerConversions != null) {
                        final int powerNeeded = ConvertAction.getPowerCost(powerConversions);
                        burn = player.getNeededBurn(powerNeeded);
                    }
                }
            }
        }
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
        if (!target.isDiggable()) {
            return false;
        }
        if (type == Hex.Type.VARIABLE || type == Hex.Type.WATER) {
            return false;
        }
        if (target == null || target.getType() == type) {
            return false;
        }
        if (resolvingCultSpades) {
            if (jump || requiredDigging > 0) {
                return false;
            }
        }
        final boolean reachable = game.isReachable(target, player);
        if (jump) {
            if (reachable || !game.isJumpable(target, player)) {
                return false;
            }
        } else if (!reachable) {
            return false;
        }
        // Partial usage of pending spades to anything but home terrain is not allowed.
        if (requiredDigging > 0 && pendingSpades > 0 && !player.allowExtraSpades) return false;
        if (pendingSpades > 1 && type != player.getHomeType() && requiredSpades < pendingSpades) return false;
        return target.getStructure() == null && requiredSpades != 0 && player.canDig(requiredDigging, jump);
    }

    @Override
    public void execute() {
        if (powerConversions != null) {
            if (burn > 0) {
                player.burn(burn);
            }
            player.convert(powerConversions);
        }
        if (jump) {
            player.useRange(true);
        }
        if (requiredDigging > 0) {
            if (pendingSpades > 0 && !player.allowExtraSpades) throw new RuntimeException("Adding extra spades not allowed");
            if (player.getFaction().getHomeType() == Hex.Type.VOLCANO) {
                player.volcanoDig(requiredDigging, cult);
            } else {
                player.dig(requiredDigging);
            }
        }
        player.useSpades(requiredSpades);
        target.setType(type);
        if (type == player.getHomeType() && !resolvingCultSpades) {
            player.addPendingBuild(target);
        }
    }

    @Override
    public boolean isFree() {
        return pendingSpades > 0 && !resolvingCultSpades;
    }

    @Override
    public String toString() {
        String result = powerConversions == null ? "" : ConvertAction.getConversionString(burn, powerConversions);
        String txt = "Transform " + target.getId() + " to " + type;
        if (requiredDigging > 0) {
            txt = "Dig " + (player.getHomeType() == Hex.Type.VOLCANO ? requiredSpades : requiredDigging) + ". " + txt;
            if (cult >= 0) {
                txt += ". -" + requiredDigging + Cults.getCultName(cult);
            }
        }
        return result.isEmpty() ? txt : result + ". " + txt;
    }
}
