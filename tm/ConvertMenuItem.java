package tm;

import tm.action.ConvertAction;

import java.awt.*;

public class ConvertMenuItem extends ActionMenuItem {

    private final Resources resources;

    ConvertMenuItem(Game game, Menu menu, String name, int shortcut, Resources resources) {
        super(game, menu, name, shortcut);
        this.resources = resources;
    }

    @Override
    public boolean canExecute(Game game) {
        return (game.phase == Game.Phase.ACTIONS || game.phase == Game.Phase.CONFIRM_ACTION) && game.getCurrentPlayer().canAffordPower(ConvertAction.getPowerCost(resources));
    }

    @Override
    protected void addListener(Game game) {
        addActionListener(l -> {
            game.resolveAction(new ConvertAction(resources, 0, 0, 0, 0));
        });
    }
}
