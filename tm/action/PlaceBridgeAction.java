package tm.action;

import tm.Game;
import tm.Hex;
import tm.Player;

public class PlaceBridgeAction extends PendingAction {

    private final int row1;
    private final int col1;
    private final int row2;
    private final int col2;
    private transient Hex hex1;
    private transient Hex hex2;

    public PlaceBridgeAction(int row1, int col1, int row2, int col2) {
        this.row1 = row1;
        this.col1 = col1;
        this.row2 = row2;
        this.col2 = col2;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        hex1 = game.getHex(row1, col1);
        hex2 = game.getHex(row2, col2);
    }

    @Override
    public boolean canExecute() {
        if (hex1 == null || hex2 == null || hex1 == hex2) return false;
        if (player.getBridgesLeft() <= 0 || !player.getPendingActions().contains(Player.PendingType.PLACE_BRIDGE)) return false;

        final Hex.Type homeType = player.getHomeType();
        if (hex1.getType() != homeType && hex2.getType() != homeType) return false;
        if ((hex1.getType() != homeType || hex1.getStructure() == null) && (hex2.getType() != homeType || hex2.getStructure() == null)) return false;

        int requiredCommonWaterNeighbors = 2;
        if (hex1.getNeighbors().size() <= 3 && hex2.getNeighbors().size() <= 3) {
            --requiredCommonWaterNeighbors;
        }
        for (Hex neighbor : hex1.getNeighbors()) {
            if (hex2.getNeighbors().contains(neighbor) && neighbor.getType() == Hex.Type.WATER) {
                --requiredCommonWaterNeighbors;
            }
        }
        return requiredCommonWaterNeighbors <= 0;
    }

    @Override
    public void execute() {
        game.getCurrentPlayer().placeBridge(hex1, hex2);
        game.checkTowns(player);
    }

    @Override
    public String toString() {
        return "Bridge " + hex1.getId() + ":" + hex2.getId();
    }
}
