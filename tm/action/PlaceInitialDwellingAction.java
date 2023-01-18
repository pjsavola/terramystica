package tm.action;

import tm.Grid;
import tm.Hex;
import tm.Player;
import tm.faction.Faction;

public class PlaceInitialDwellingAction extends Action {

    private final Grid grid;
    private final int row;
    private final int col;

    public PlaceInitialDwellingAction(Player player, Grid grid, int row, int col) {
        super(player);
        this.grid = grid;
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean canExecute() {
        final Hex hex = grid.getHex(row, col);
        return hex != null && hex.getType() == player.getFaction().getHomeType();
    }

    @Override
    public void execute() {
        final Hex hex = grid.getHex(row, col);
        hex.setStructure(Hex.Structure.DWELLING);
        player.placeInitialDwelling();
    }
}
