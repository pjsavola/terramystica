package tm.action;

import tm.Game;
import tm.Player;

public class PassAction extends Action {

    @Override
    public boolean canExecute() {
        return game.getRound() == 6;
    }

    @Override
    public void execute() {
        player.pass();
    }

    @Override
    public boolean isPass() {
        return true;
    }

    @Override
    public String toString() {
        return "Pass";
    }
}
