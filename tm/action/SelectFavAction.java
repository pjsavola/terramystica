package tm.action;

import tm.Game;

public class SelectFavAction extends Action {

    private final int fav;

    public SelectFavAction(int fav) {
        this.fav = fav;
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.CONFIRM_ACTION;
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
    public boolean isFree() {
        return true;
    }

    @Override
    public String toString() {
        return "+FAV" + fav;
    }
}
