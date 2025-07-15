package tm.action;

import tm.Game;
import tm.Hex;
import tm.faction.Riverwalkers;

public class PickColorAction extends Action {

    private final Hex.Type type;

    public PickColorAction(Hex.Type type) {
        this.type = type;
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.PICK_FACTIONS;
    }

    @Override
    public boolean canExecute() {
        if (type == null || game.getSelectedOrdinals().contains(type.ordinal())) return false;
        if (type == Hex.Type.ICE || type == Hex.Type.VARIABLE || type == Hex.Type.VOLCANO) return false;

        final boolean ice = player.getFaction().getHomeType() == Hex.Type.ICE;
        final boolean volcano = player.getFaction().getHomeType() == Hex.Type.VOLCANO;
        final boolean variable = player.getFaction().getHomeType() == Hex.Type.VARIABLE;
        if (ice && game.getIceColor() != null) return false;
        if (volcano && game.getVolcanoColor() != null) return false;
        if (variable && game.getVariableColor() != null) return false;

        return player.getFaction() != null && (ice || volcano || variable);
    }

    @Override
    public void execute() {
        switch (player.getFaction().getHomeType()) {
            case ICE -> game.setIceColor(type);
            case VOLCANO -> game.setVolcanoColor(type);
            case VARIABLE -> {
                game.setVariableColor(type);
                player.initialUnlockedTerrainIndex = type.ordinal();
                if (player.getFaction() instanceof Riverwalkers) {
                    player.unlockedTerrain[player.initialUnlockedTerrainIndex] = true;
                }
            }
        }
    }

    @Override
    public boolean isPass() {
        return true;
    }

    @Override
    public String toString() {
        return "Pick-color " + type;
    }
}
