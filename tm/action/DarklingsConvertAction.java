package tm.action;

import tm.Game;
import tm.Resources;

public class DarklingsConvertAction extends PendingAction {

    private final int workersToPriests;

    public DarklingsConvertAction(int workersToPriests) {
        this.workersToPriests = workersToPriests;
    }

    @Override
    public boolean canExecute() {
        return workersToPriests > 0 &&
                workersToPriests <= 3 &&
                player.getWorkers() > 0 &&
                player.hasPendingPriestToWorkerConversions();
    }

    @Override
    public void execute() {
        player.addIncome(Resources.fromPriests(workersToPriests));
        player.pay(Resources.fromWorkers(workersToPriests));
    }

    @Override
    public String toString() {
        return "Convert " + workersToPriests + "w to " + workersToPriests + "p";
    }
}
