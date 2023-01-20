package tm.action;

import tm.Game;
import tm.Player;
import tm.faction.Faction;

public class SelectFactionAction extends Action {

    private final Faction faction;

    public SelectFactionAction(Faction faction) {
        this.faction = faction;
    }

    @Override
    public boolean validatePhase() {
        return game.phase == Game.Phase.INITIAL_DWELLINGS;
    }

    @Override
    public boolean canExecute() {
        return player.getFaction() == null;
    }

    @Override
    public void execute() {
        player.selectFaction(faction, 20);
    }

    public boolean needsConfirm() {
        return true;
    }

    @Override
    public String toString() {
        return "Select " + faction.getName();
    }
}
