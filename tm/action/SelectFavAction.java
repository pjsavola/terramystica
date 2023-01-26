package tm.action;

import tm.Game;

public class SelectFavAction extends PendingAction {

    private final int fav;

    public SelectFavAction(int fav) {
        this.fav = fav;
    }

    @Override
    public boolean canExecute() {
        return game.canSelectFav(fav);
    }

    @Override
    public void execute() {
        game.selectFav(player, fav);
    }

    @Override
    public String toString() {
        return "+FAV" + fav;
    }
}
