package tm.action;

import tm.Game;
import tm.Hex;
import tm.Resources;
import tm.faction.Riverwalkers;

public class UnlockTerrainAction extends Action {

    private final Hex.Type type;

    public UnlockTerrainAction(Hex.Type type) {
        this.type = type;
    }

    @Override
    public boolean validatePhase() {
        return game.resolvingTerrainUnlock || (game.resolvingCultSpades() && player.pendingTerrainUnlock > 0);
    }

    @Override
    public boolean canExecute() {
        if (!(player.getFaction() instanceof Riverwalkers)) return false;
        if (player.pendingTerrainUnlock == 0) return false;
        if (type != null) {
            final int ordinal = type.ordinal();
            if (ordinal >= 7) return false;
            if (player.unlockedTerrain[ordinal]) return false;

            return player.canAfford(game.isHomeType(type) ? Resources.c2 : Resources.c1);
        }
        return true;
    }

    @Override
    public void execute() {
        if (type == null) {
            player.addPriests(1);
        } else {
            player.maxPriests = Math.min(7, player.maxPriests + 1);
            player.pay(game.isHomeType(type) ? Resources.c2 : Resources.c1);
            player.unlockedTerrain[type.ordinal()] = true;
        }
        --player.pendingTerrainUnlock;
    }

    @Override
    public boolean isPass() {
        return true;
    }

    @Override
    public String toString() {
        return "Unlock-terrain " + (type == null ? "gain-priest" : type);
    }
}
