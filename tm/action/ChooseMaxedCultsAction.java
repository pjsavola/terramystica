package tm.action;

import tm.Cults;
import tm.Game;
import tm.Player;
import tm.faction.Auren;

import java.util.Arrays;

public class ChooseMaxedCultsAction extends Action {

    private final boolean[] cultsToMax;

    public ChooseMaxedCultsAction(boolean[] cultsToMax) {
        this.cultsToMax = cultsToMax;
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.CONFIRM_ACTION && player.getPendingActions().contains(Player.PendingType.CHOOSE_CULTS);
    }

    public static boolean actionNeeded(Game game) {
        final Player player = game.getCurrentPlayer();
        if (player.getRemainingKeys() > 0) {
            return false;
        }
        int usedKeys = 0;
        int neededKeys = 0;
        for (int i = 0; i < 4; ++i) {
            if (player.maxedCults[i]) {
                ++neededKeys;
                if (player.getCultSteps(i) >= 10) {
                    ++usedKeys;
                }
            }
        }
        return usedKeys > 0 && neededKeys > usedKeys;
    }

    @Override
    public boolean canExecute() {
        if (player.getRemainingKeys() > 0) {
            return false;
        }
        int usedKeys = 0;
        int neededKeys = 0;
        for (int i = 0; i < 4; ++i) {
            if (player.maxedCults[i]) {
                if (player.getCultSteps(i) >= 10) {
                    ++usedKeys;
                }
                if (cultsToMax[i]) {
                    ++neededKeys;
                }
            } else if (cultsToMax[i]) {
                return false;
            }
        }
        return usedKeys > 0 && usedKeys == neededKeys;
    }

    @Override
    public void execute() {
        final int[] newSteps = new int[4];
        for (int i = 0; i < 4; ++i) {
            if (player.maxedCults[i]) {
                newSteps[i] = cultsToMax[i] ? 10 : 9;
            } else {
                newSteps[i] = player.getCultSteps(i);
            }
        }
        player.setCultSteps(newSteps);
    }

    @Override
    public boolean isFree() {
        return true;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < 4; ++i) {
            if (cultsToMax[i]) {
                s += "Max " + Cults.getCultName(i);
            }
        }
        return s;
    }
}
