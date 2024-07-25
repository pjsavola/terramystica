package tm.action;

public class PassAction extends Action {

    @Override
    public boolean canExecute() {
        return game.getRound() == 6;
    }

    @Override
    public void execute() {
        player.pass();
        game.finalPass(player);
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
