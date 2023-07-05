package tm;

import java.awt.*;

public abstract class ActionMenuItem extends MenuItem {

    protected ActionMenuItem(String name) {
        super(name);
    }

    public abstract boolean canExecute(Game game);
}
