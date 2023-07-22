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
        return game.phase == Game.Phase.PICK_FACTIONS;
    }

    @Override
    public boolean canExecute() {
        return player.getFaction() == null && game.getSelectableFactions().anyMatch(f -> f == faction);
    }

    @Override
    public void execute() {
        player.selectFaction(faction);
    }

    public boolean needsConfirm() {
        return true;
    }

    @Override
    public String toString() {
        return "Select " + faction.getName();
    }
}
