package tm.action;

import tm.Player;
import tm.faction.Faction;

public class SelectFactionAction extends Action {

    private final Faction faction;

    public SelectFactionAction(Player player, Faction faction) {
        super(player);
        this.faction = faction;
    }

    @Override
    public boolean canExecute() {
        return player.getFaction() == null;
    }

    @Override
    public void execute() {
        player.selectFaction(faction, 20);
    }
}
