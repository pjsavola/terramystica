package tm.action;

import tm.Game;

public class SelectTownAction extends PendingAction {

    private final int town;

    public SelectTownAction(int town) {
        this.town = town;
    }

    @Override
    public boolean canExecute() {
        return game.canSelectTown(town);
    }

    @Override
    public void execute() {
        game.selectTown(player, town);
    }

    @Override
    public String toString() {
        return "+TW" + town;
    }
}
