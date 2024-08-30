package tm.action;

import tm.Cults;
import tm.Game;
import tm.Player;
import tm.faction.Acolytes;
import tm.faction.Auren;

public class ShapeshifterPowerAction extends Action {

    private final boolean response;

    public ShapeshifterPowerAction(boolean response) {
        this.response = response;
    }

    @Override
    public boolean canExecute() {
        return !response || player.getPoints() > 0;
    }

    @Override
    public void execute() {
        if (response) {
            player.addTokenToBowl3();
        }
    }

    @Override
    public String toString() {
        return response ? "Gain P3 for VP" : "";
    }
}
