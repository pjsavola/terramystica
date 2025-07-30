package tm.action;

import tm.Game;
import tm.Hex;
import tm.faction.Faction;
import tm.faction.Riverwalkers;

public class PickColorAction extends Action {

    private final Hex.Type type;

    public PickColorAction(Hex.Type type) {
        this.type = type;
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.PICK_FACTIONS || game.hasPendingColorPick();
    }

    @Override
    public boolean canExecute() {
        if (type == null || game.getSelectedOrdinals().contains(type.ordinal())) return false;
        if (type == Hex.Type.ICE || type == Hex.Type.VARIABLE || type == Hex.Type.VOLCANO) return false;

        final Faction faction = player.getFaction();
        final boolean ice = faction != null && faction.getHomeType() == Hex.Type.ICE;
        final boolean volcano = faction != null && faction.getHomeType() == Hex.Type.VOLCANO;
        final boolean variable = faction != null && faction.getHomeType() == Hex.Type.VARIABLE;
        if (ice && game.getIceColor() != null) return false;
        if (volcano && game.getVolcanoColor() != null) return false;
        if (variable && game.getVariableColor() != null) return false;

        return player.getFaction() != null && (ice || volcano || variable);
    }

    @Override
    public void execute() {
        switch (player.getFaction().getHomeType()) {
            case ICE -> {
                player.pendingColorPick = false;
                game.setIceColor(type);
            }
            case VOLCANO -> game.setVolcanoColor(type);
            case VARIABLE -> {
                player.pendingColorPick = false;
                game.setVariableColor(type);
                player.initialUnlockedTerrainIndex = type.ordinal();
                if (player.getFaction() instanceof Riverwalkers) {
                    player.unlockedTerrain[player.initialUnlockedTerrainIndex] = true;
                }
            }
        }
    }

    @Override
    public boolean isFree() {
        return player.getFaction().getHomeType() != Hex.Type.VOLCANO;
    }

    @Override
    public boolean isPass() {
        return player.getFaction().getHomeType() == Hex.Type.VOLCANO;
    }

    @Override
    public String toString() {
        return "Pick-color " + type;
    }
}
