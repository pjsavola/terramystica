package tm.action;

import tm.faction.Swarmlings;

public class SwarmlingsFreeTradingPostAction extends Action {

    @Override
    public boolean canExecute() {
        return player.getFaction() instanceof Swarmlings && player.hasStronghold() && !player.usedFactionAction;
    }

    @Override
    public void execute() {
        player.pendingFreeTradingPost = true;
        player.usedFactionAction = true;
    }

    @Override
    public String toString() {
        return "Action ACTS";
    }
}
