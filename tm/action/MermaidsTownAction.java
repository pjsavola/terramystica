package tm.action;

import tm.Game;
import tm.Hex;
import tm.faction.Engineers;
import tm.faction.Mermaids;

public class MermaidsTownAction extends Action {

    private final Hex hex;

    public MermaidsTownAction(Hex hex) {
        this.hex = hex;
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION;
    }

    @Override
    public boolean canExecute() {
        return game.pendingTownPlacement && player.getFaction() instanceof Mermaids && game.canPlaceMermaidTown(hex, player);
    }

    @Override
    public void execute() {
        game.pendingTownPlacement = false;
        game.placeMermaidTown(hex, player);
    }

    @Override
    public String toString() {
        return "Action ACTM " + hex.getId();
    }
}
