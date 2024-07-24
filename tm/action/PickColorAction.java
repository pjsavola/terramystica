package tm.action;

import tm.Game;
import tm.Hex;

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

        return player.getFaction() != null && (player.getFaction().getHomeType() == Hex.Type.ICE || player.getFaction().getHomeType() == Hex.Type.VOLCANO);
    }

    @Override
    public void execute() {
        switch (player.getFaction().getHomeType()) {
            case ICE -> game.setIceColor(type);
            case VOLCANO -> game.setVolcanoColor(type);
        }
    }

    @Override
    public boolean isPass() {
        return true;
    }

    @Override
    public String toString() {
        return "Pick " + type;
    }
}
