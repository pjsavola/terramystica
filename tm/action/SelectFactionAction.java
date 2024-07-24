package tm.action;

import tm.Game;
import tm.GameData;
import tm.Hex;
import tm.Player;
import tm.faction.Faction;

public class SelectFactionAction extends Action {

    private final int factionIdx;
    private final Hex.Type color;
    private transient Faction faction;

    public SelectFactionAction(int factionIdx, Hex.Type color) {
        this.factionIdx = factionIdx;
        this.color = color;
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
        if (faction.getHomeType() == Hex.Type.ICE || faction.getHomeType() == Hex.Type.VOLCANO) {
            if (color == null) return false;
            if (game.getSelectedOrdinals().contains(color.ordinal())) return false;
        }
        return player.getFaction() == null && game.getSelectableFactions().anyMatch(f -> f == faction);
    }

    @Override
    public void execute() {
        if (faction.getHomeType() == Hex.Type.ICE) game.setIceColor(color);
        if (faction.getHomeType() == Hex.Type.VOLCANO) game.setVolcanoColor(color);
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
        return "Select " + GameData.allFactions.get(factionIdx).getName();
    }
}
