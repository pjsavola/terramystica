package tm;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowChanger extends WindowAdapter {
    private final JFrame frame;
    private final JPanel mainPanel;

    public WindowChanger(JFrame frame, JPanel mainPanel) {
        this.frame = frame;
        this.mainPanel = mainPanel;
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        if (frame.getContentPane() == mainPanel) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setJMenuBar(null);
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setContentPane(mainPanel);
            frame.pack();
        }
    }
}
