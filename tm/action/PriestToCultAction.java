package tm.action;

import tm.Cults;
import tm.Game;
import tm.Player;
import tm.Resources;

public class PriestToCultAction extends Action {

    private final int cult;
    private final int amount;
    private transient Resources powerConversions;
    private transient int burn;

    public PriestToCultAction(int cult, int amount) {
        this.cult = cult;
        this.amount = amount;
    }

    @Override
    public void setData(Game game, Player player) {
        super.setData(game, player);
        final Resources resources = player.getResources();
        if (resources.priests == 0) {
            powerConversions = Resources.p1;
            final int powerNeeded = ConvertAction.getPowerCost(powerConversions);
            burn = player.getNeededBurn(powerNeeded);
        }
    }

    public boolean canExecute() {
        return cult >= 0 && cult < 4 && player.canSendPriestToCult() && game.cultPanel.isCultSpotFree(cult, amount);
    }

    public void execute() {
        if (powerConversions != null) {
            if (burn > 0) {
                player.burn(burn);
            }
            player.convert(powerConversions);
        }
        game.cultPanel.sendPriestToCult(player, cult, amount);
    }

    @Override
    public String toString() {
        String result = powerConversions == null ? "" : ConvertAction.getConversionString(burn, powerConversions);
        String sendStr = "Send P to " + Cults.getCultName(cult) + " for " + amount;
        return result.isEmpty() ? sendStr : result + ". " + sendStr;
    }
}
