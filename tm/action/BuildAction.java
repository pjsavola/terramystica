package tm.action;

import tm.Game;
import tm.Hex;
import tm.Player;
import tm.faction.ChaosMagicians;
import tm.faction.Darklings;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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
        if (hex == null) throw new RuntimeException("Invalid hex");

        if (structure == Hex.Structure.TRADING_POST) {
            expensive = true;
            for (Hex n : hex.getNeighbors()) {
                if (n.getStructure() != null && n.getType() != player.getHomeType()) {
                    expensive = false;
                    break;
                }
            }
        }
    }

    @Override
    public final boolean validatePhase() {
        if (game.phase == Game.Phase.CONFIRM_ACTION) {
            return player.getPendingActions().contains(Player.PendingType.BUILD);
        }
        return super.validatePhase();
    }

    @Override
    public boolean canExecute() {
        final Hex hex = game.getHex(row, col);
        if (hex.getType() != player.getHomeType()) return false;
        if (structure.getParent() != hex.getStructure()) return false;
        if (structure == Hex.Structure.DWELLING) {
            if (player.getPendingActions().contains(Player.PendingType.BUILD)) {
                return player.hasPendingBuild(hex) && player.canBuildDwelling(false);
            }
            boolean jump = false;
            if (!game.isReachable(hex, player)) {
                if (!game.isJumpable(hex, player)) {
                    return false;
                }
                jump = true;
            }
            return player.canBuildDwelling(jump);
        }

        return switch (structure) {
            case TRADING_POST -> player.canBuildTradingPost(expensive);
            case TEMPLE -> player.canBuildTemple();
            case STRONGHOLD -> player.canBuildStronghold();
            case SANCTUARY -> player.canBuildSanctuary();
            default -> throw new IllegalStateException("Unexpected value: " + structure);
        };
    }

    @Override
    public void execute() {
        final Hex hex = game.getHex(row, col);
        if (structure == Hex.Structure.DWELLING) {
            if (player.getPendingActions().contains(Player.PendingType.BUILD)) {
                player.clearPendingBuilds();
            } else if (!game.isReachable(hex, player)) {
                player.useRange(false);
            }
        }
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
            if (n.getStructure() != null && n.getType() != player.getHomeType()) {
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
