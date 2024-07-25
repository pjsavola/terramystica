package tm.action;

import tm.Game;
import tm.Player;

import java.util.HashSet;
import java.util.Set;

public class ForfeitAction extends Action {

    private transient Set<Player.PendingType> types;
    private transient int spadeCount;

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        types = new HashSet<>(player.getSkippablePendingActions());
        if (types.contains(Player.PendingType.USE_SPADES)) {
            spadeCount = player.getPendingSpades();
        }
    }

    @Override
    public boolean validatePhase() {
        return (game.phase == Game.Phase.ACTIONS && spadeCount > 0) || game.phase == Game.Phase.CONFIRM_ACTION;
    }

    @Override
    public boolean canExecute() {
        return !types.isEmpty();
    }

    @Override
    public void execute() {
        player.clearPendingActions(types);
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder();
        for (Player.PendingType type : types) {
            switch (type) {
                case USE_SPADES -> txt.append("-").append("spade");
                case PLACE_BRIDGE -> txt.append("-bridge");
            }
        }
        return txt.toString();
    }
}
