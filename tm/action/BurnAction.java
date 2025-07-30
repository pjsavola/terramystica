package tm.action;

import tm.Game;

public class BurnAction extends Action {

    private final int amount;

    public BurnAction(int amount) {
        this.amount = amount;
    }

    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS || (game.phase == Game.Phase.CONFIRM_ACTION && game.factionsPicked());
    }

    @Override
    public boolean canExecute() {
        return amount <= player.getMaxBurn();
    }

    @Override
    public void execute() {
        player.burn(amount);
    }

    @Override
    public boolean isFree() {
        return true;
    }

    @Override
    public String toString() {
        return "Burn " + amount;
    }
}
