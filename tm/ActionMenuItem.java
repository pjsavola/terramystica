package tm;

import java.awt.*;

public abstract class ActionMenuItem extends MenuItem {

    protected ActionMenuItem(Menu menu, String name) {
        super(name);
        menu.add(this);
        addListener();
    }

    public abstract boolean canExecute(Game game);
    protected abstract void addListener();
}
