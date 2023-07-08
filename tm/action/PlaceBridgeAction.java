package tm.action;

import tm.Hex;
import tm.Player;

public class PlaceBridgeAction extends PendingAction {

    private final Hex hex1;
    private final Hex hex2;

    public PlaceBridgeAction(Hex hex1, Hex hex2) {
        this.hex1 = hex1;
        this.hex2 = hex2;
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
