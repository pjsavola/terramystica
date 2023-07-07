package tm.action;

import tm.Game;
import tm.Grid;
import tm.Hex;
import tm.Player;
import tm.faction.Faction;

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
        return hex.getType() == player.getHomeType() && hex.getStructure() == null;
    }

    @Override
    public void execute() {
        game.getHex(row, col).setStructure(Hex.Structure.DWELLING);
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
