package tm.action;

import tm.Game;
import tm.Hex;
import tm.Player;
import tm.faction.Engineers;
import tm.faction.Mermaids;

public class MermaidsTownAction extends Action {

    private final int row;
    private final int col;
    private transient Hex hex;

    public MermaidsTownAction(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        this.hex = game.getHex(row, col);
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION;
    }

    @Override
    public boolean canExecute() {
        return player.getFaction() instanceof Mermaids && game.canPlaceMermaidTown(hex, player);
    }

    @Override
    public void execute() {
        game.placeMermaidTown(hex, player);
    }

    @Override
    public boolean isFree() {
        return true;
    }

    @Override
    public String toString() {
        return "Connect " + hex.getId();
    }
}
