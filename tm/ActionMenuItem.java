package tm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public abstract class ActionMenuItem extends JMenuItem {

    ActionMenuItem(Game game, JMenu menu, String name) {
        super(name);
        menu.add(this);
        addActionListener(l -> execute(game));
    }

    ActionMenuItem(Game game, JMenu menu, String name, int shortcut) {
        super(name, shortcut);
        setAccelerator(KeyStroke.getKeyStroke(shortcut, 0));
        menu.add(this);
        addActionListener(l -> execute(game));
    }

    public abstract boolean canExecute(Game game);
    protected abstract void execute(Game game);
}
