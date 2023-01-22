package tm.action;

import tm.Game;
import tm.Hex;
import tm.Player;
import tm.faction.ChaosMagicians;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildAction extends Action {

    private final int row;
    private final int col;
    private final Hex.Structure structure;
    private boolean expensive;

    public BuildAction(int row, int col, Hex.Structure structure) {
        this.row = row;
        this.col = col;
        this.structure = structure;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        final Hex hex = game.getHex(row, col);
        if (structure == Hex.Structure.TRADING_POST) {
            expensive = true;
            for (Hex n : hex.getNeighbors()) {
                if (n.getStructure() != null && n.getType() != player.getFaction().getHomeType()) {
                    expensive = false;
                    break;
                }
            }
        }
    }

    @Override
    public boolean canExecute() {
        // TODO: Reachability
        final Hex hex = game.getHex(row, col);
        if (hex.getType() != player.getFaction().getHomeType()) return false;
        if (structure.getParent() != hex.getStructure()) return false;

        return switch (structure) {
            case DWELLING -> player.canBuildDwelling();
            case TRADING_POST -> player.canBuildTradingPost(expensive);
            case TEMPLE -> player.canBuildTemple();
            case STRONGHOLD -> player.canBuildStronghold();
            case SANCTUARY -> player.canBuildSanctuary();
        };
    }

    @Override
    public void execute() {
        final Hex hex = game.getHex(row, col);
        hex.setStructure(structure);
        switch (structure) {
            case DWELLING -> player.buildDwelling();
            case TRADING_POST -> player.buildTradingPost(expensive);
            case TEMPLE -> player.buildTemple();
            case STRONGHOLD -> player.buildStronghold();
            case SANCTUARY -> player.buildSanctuary();
        }
    }

    @Override
    public void confirmed() {
        final Map<Hex.Type, Integer> leech = new HashMap<>();
        final Hex hex = game.getHex(row, col);
        for (Hex n : hex.getNeighbors()) {
            if (n.getStructure() != null && n.getType() != player.getFaction().getHomeType()) {
                final int power = switch (n.getStructure()) {
                    case DWELLING -> 1;
                    case TRADING_POST -> 2;
                    case TEMPLE -> 2;
                    case STRONGHOLD -> 3;
                    case SANCTUARY -> 3;
                };
                leech.put(n.getType(), leech.getOrDefault(n.getType(), 0) + power);
            }
        }
        game.leechTriggered(leech);
    }

    @Override
    public String toString() {
        final Hex hex = game.getHex(row, col);
        final String id = hex.getId();

        // Auto pick favs in rare cases
        final StringBuilder autoPicks = new StringBuilder();
        if (structure == Hex.Structure.TEMPLE || structure == Hex.Structure.SANCTUARY) {
            final List<Integer> favOptions = game.getSelectableFavs(player);
            final int newFavs = Math.max(favOptions.size(), player.getFaction() instanceof ChaosMagicians ? 2 : 1);
            if (newFavs == favOptions.size()) {
                favOptions.forEach(fav -> autoPicks.append(". +FAV").append(fav));
            }
        }

        return structure == Hex.Structure.DWELLING ? ("Build " + id) : ("Upgrade " + id + " to " + structure.getAbbrevation() + autoPicks);
    }
}
