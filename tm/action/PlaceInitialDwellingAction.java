package tm.action;

import tm.Game;
import tm.Hex;

public class PlaceInitialDwellingAction extends Action {

    private final int row;
    private final int col;

    public PlaceInitialDwellingAction(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.INITIAL_DWELLINGS;
    }

    @Override
    public boolean canExecute() {
        final Hex hex = game.getHex(row, col);
        if (hex.getStructure() != null) return false;

        if (player.getHomeType() == Hex.Type.ICE) {
            return game.getIceColor() == hex.getType();
        } else if (player.getHomeType() == Hex.Type.VOLCANO) {
            return game.getVolcanoColor() == hex.getType();
        } else {
            return hex.getType() == player.getHomeType();
        }
    }

    @Override
    public void execute() {
        final Hex hex = game.getHex(row, col);
        hex.setStructure(Hex.Structure.DWELLING);
        if (player.getHomeType() == Hex.Type.ICE) {
            hex.setType(Hex.Type.ICE);
        } else if (player.getHomeType() == Hex.Type.VOLCANO) {
            hex.setType(Hex.Type.VOLCANO);
        }
        player.placeInitialDwelling();
    }

    @Override
    public boolean isPass() {
        return true;
    }

    @Override
    public boolean needsConfirm() {
        return false;
    }

    @Override
    public String toString() {
        final Hex hex = game.getHex(row, col);
        final String id = hex.getId();
        return "Build " + id;
    }
}
