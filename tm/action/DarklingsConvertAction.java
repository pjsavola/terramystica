package tm.action;

import tm.Player;

public class DarklingsConvertAction extends PendingAction {

    private final int workersToPriests;

    public DarklingsConvertAction(int workersToPriests) {
        this.workersToPriests = workersToPriests;
    }

    @Override
    public boolean canExecute() {
        return workersToPriests >= 0 &&
                workersToPriests <= 3 &&
                player.getWorkers() >= workersToPriests &&
                player.getPendingActions().contains(Player.PendingType.CONVERT_W2P);
    }

    @Override
    public void execute() {
        player.convertWorkersToPriests(workersToPriests);
    }

    @Override
    public String toString() {
        if (workersToPriests == 0) return "";
        return "Convert " + workersToPriests + "w to " + workersToPriests + "p";
    }
}
