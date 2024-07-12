package tm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.*;

public class WindowChanger extends WindowAdapter {
    private final JFrame frame;
    private final JPanel mainPanel;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            final int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit the game?", "Confirm exit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
            if (option != JOptionPane.OK_OPTION) {
                return;
            }
            frame.setJMenuBar(null);
            frame.setContentPane(mainPanel);
            for (Window w : frame.getOwnedWindows()) {
                w.setVisible(false);
            }
            frame.pack();
            // For some reason we need delayed repaint or otherwise repaint sometimes happens too early and doesn't do anything.
            final Runnable task = frame::repaint;
            scheduler.schedule(task, 100, TimeUnit.MILLISECONDS);
        }
    }
}
