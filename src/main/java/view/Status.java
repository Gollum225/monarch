package view;

import model.Repository;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Responsible for creating a window with actual information regarding the progress.
 */
public class Status {
    private final JFrame frame;
    private final JPanel panel;
    private JLabel finishedReposLabel;
    private final Map<Repository, JLabel> statusBars;
    private final Map<Repository, Integer> progress = new HashMap<>();
    private final int maxBlocks;
    private int finishedRepos = 0;

    /**
     * Creates a new Status window.
     * @param steps how many individual processing steps each element has
     */
    public Status(int steps) {
        frame = new JFrame("Status");
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        statusBars = new LinkedHashMap<>();

        frame.add(panel);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setVisible(true);
        maxBlocks = steps;
        finishedReposLabel = new JLabel("Finished Repos: 0");
        panel.add(finishedReposLabel);
        panel.revalidate();
        panel.repaint();

    }

    /**
     * Adds a new bar.
     *
     * @param repo {@link Repository} for the bar.
     */
    public void addStatusBar(Repository repo) {
        if (statusBars.containsKey(repo)) {
            return;
        }
        progress.putIfAbsent(repo, 0);

        JLabel label = new JLabel(getProgressString(0) + " " + repo.getIdentifier() + ": ");
        statusBars.put(repo, label);
        panel.add(label, 1);
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Lets the bar progress one step further.
     * @param repo to identify the right bar.
     * @param ruleName to display the current processing rule.
     */
    public void updateStatusBar(Repository repo, String ruleName) {
        JLabel label = statusBars.get(repo);
        if (label != null) {
            try {
                progress.replace(repo, progress.get(repo) + 1);
            } catch (Exception e) {
                panel.remove(label);
                return;
            }

            //String text = label.getText();
            //int current = text.lastIndexOf("█") + 1; // Anzahl gefüllter Blöcke
            //if (current < maxBlocks) {
                label.setText(getProgressString(progress.get(repo)) + " " + ruleName + ": " + repo.getRepositoryName() + ", " + repo.getOwner());
            //}
        }
        panel.revalidate();
        panel.repaint();

    }

    /**
     * Sets the bar of a repo to finish. It will be displayed in green.
     *
     * @param repo to finish
     */
    public void removeStatusBar(Repository repo) {
        JLabel label = statusBars.get(repo);
        if (label != null) {
            label.setText(getProgressString(progress.get(repo)) + " " + repo.getRepositoryName() + ", " + repo.getOwner());
            label.setForeground(new Color(0, 100, 0));
        }
        finishedRepos++;
        finishedReposLabel.setText("Finished Repos: " + finishedRepos);
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Closes the window.
     */
    public void finish() {
        frame.dispose();
    }

    /**
     * Calculates the progress of the bar.
     *
     * @param filled number of completed steps.
     * @return the calculated String
     */
    private String getProgressString(int filled) {
        return "█".repeat(2*filled) + "░".repeat(2*(maxBlocks - filled));
    }

}
