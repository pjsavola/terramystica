package tm.action;

public class AdvanceAction extends Action {

    private final boolean dig;

    public AdvanceAction(boolean dig) {
        this.dig = dig;
    }

    @Override
    public boolean canExecute() {
        return dig ? player.canAdvanceDigging() : player.canAdvanceShipping();
    }

    @Override
    public void execute() {
        if (dig) player.advanceDigging();
        else player.advanceShipping();
    }

    @Override
    public String toString() {
        return dig ? "Advance dig" : "Advance ship";
    }
}
