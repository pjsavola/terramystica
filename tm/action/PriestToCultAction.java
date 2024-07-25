package tm.action;

import tm.Cults;

public class PriestToCultAction extends Action {

    private final int cult;
    private final int amount;

    public PriestToCultAction(int cult, int amount) {
        this.cult = cult;
        this.amount = amount;
    }

    public boolean canExecute() {
        return cult >= 0 && cult < 4 && player.canSendPriestToCult() && game.cultPanel.isCultSpotFree(cult, amount);
    }

    public void execute() {
        game.cultPanel.sendPriestToCult(player, cult, amount);
    }

    @Override
    public String toString() {
        return "Send P to " + Cults.getCultName(cult) + " for " + amount;
    }
}
