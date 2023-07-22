package tm.action;

import tm.Game;
import tm.Hex;
import tm.Player;
import tm.faction.Giants;
import tm.faction.Nomads;

public class SandstormAction extends Action {

    private int row;
    private int col;
    private transient Hex target;

    public SandstormAction(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        target = game.getHex(row, col);
    }

    @Override
    public final boolean validatePhase() {
        return game.phase == Game.Phase.CONFIRM_ACTION && player.getPendingActions().contains(Player.PendingType.SANDSTORM);
    }

    @Override
    public boolean canExecute() {
        return game.getSandstormTiles(player).contains(target);
    }

    @Override
    public void execute() {
        player.pendingSandstorm = false;
        target.setType(player.getHomeType());
        player.addPendingBuild(target);
    }

    @Override
    public String toString() {
        return "Transform " + target.getId() + " to " + player.getHomeType();
    }
}
