package tm;

import java.awt.*;

public abstract class ActionMenuItem extends MenuItem {

    ActionMenuItem(Game game, Menu menu, String name) {
        super(name);
        menu.add(this);
        addActionListener(l -> execute(game));
    }

    ActionMenuItem(Game game, Menu menu, String name, int shortcut) {
        super(name, new MenuShortcut(shortcut));
        menu.add(this);
        addActionListener(l -> execute(game));
    }

    public abstract boolean canExecute(Game game);
    protected abstract void execute(Game game);
}
