package tm;

import java.awt.*;

public abstract class ActionMenuItem extends MenuItem {

    ActionMenuItem(Game game, Menu menu, String name) {
        super(name);
        menu.add(this);
        addListener(game);
    }

    ActionMenuItem(Game game, Menu menu, String name, int shortcut) {
        super(name, new MenuShortcut(shortcut));
        menu.add(this);
        addListener(game);
    }

    public abstract boolean canExecute(Game game);
    protected abstract void addListener(Game game);
}
