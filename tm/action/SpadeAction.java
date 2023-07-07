package tm.action;

import tm.PowerActions;

public class SpadeAction extends Action {

    @Override
    public boolean canExecute() {
        return player.getBon() == 1 && !game.bonUsed[0];
    }

    @Override
    public void execute() {
        player.addSpades(1);
        game.bonUsed[0] = true;
    }

    @Override
    public String toString() {
        return "Action BON1";
    }
}
