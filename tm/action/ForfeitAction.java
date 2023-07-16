package tm.action;

import tm.Game;
import tm.Hex;
import tm.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ForfeitAction extends PendingAction {

    private Set<Player.PendingType> types;
    private int spadeCount;

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        types = new HashSet<>(player.getSkippablePendingActions());
        if (types.contains(Player.PendingType.USE_SPADES)) {
            spadeCount = player.getPendingSpades();
        }
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
