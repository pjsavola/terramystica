package tm;

import java.util.List;

public class AIUtil {
    public static int getPowerActionThreat(Player p, List<Player> turnOrder, int act) {
        int canTake = 0;
        for (Player player : turnOrder) {
            if (player == p) continue;

            final int requiredPower = PowerActions.getRequiredPower(player, act);
            if (player.canAffordPower(requiredPower)) {
                ++canTake;
            }
        }
        return canTake;
    }

    public static int getCultThreat(Player p, List<Player> turnOrder, Game game, int cult) {
        final int contestedSpot = game.cultPanel.isCultSpotFree(cult, 3) ? 3 : (game.cultPanel.isCultSpotFree(cult, 2) ? 2 : 1);
        if (contestedSpot == 1) return 0;

        final int spotCount = game.cultPanel.getFreeCultSpotCount(cult, contestedSpot);
        int canSend = 0;
        for (Player player : turnOrder) {
            if (player == p) continue;

            if (player.canSendPriestToCult()) {
                ++canSend;
            }
        }
        if (spotCount >= canSend) {
            return 0;
        }
        return canSend;
    }

    public static int getDigThreat(Player p, List<Player> turnOrder, Game game, Hex hex) {
        if (hex.getStructure() != null) return 0;
        if (hex.getType() == Hex.Type.ICE) return 0;
        if (hex.getType() == Hex.Type.VOLCANO) return 0;

        for (Player player : turnOrder) {
            if (player == p) continue;
/*
            final Hex.Type effectiveType = player.getHomeType() == Hex.Type.ICE ? game.getIceColor() : player.getHomeType();

            if (game.isReachable(hex, player)) {

            } else if (game.isJumpable(hex, player)) {

                //player.canUseRange()
            }
            if (player.canSendPriestToCult()) {
                //++canSend;
            }
 */
        }
        return 0;
    }
}
