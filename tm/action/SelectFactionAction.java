package tm.action;

import tm.Game;
import tm.GameData;
import tm.Player;
import tm.faction.Faction;

public class SelectFactionAction extends Action {

    private final int factionIdx;
    private transient Faction faction;

    public SelectFactionAction(int factionIdx) {
        this.factionIdx = factionIdx;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        faction = GameData.allFactions.get(factionIdx);
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
    public boolean isPass() {
        return true;
    }

    @Override
    public String toString() {
        return "Select " + faction.getName();
    }
}
