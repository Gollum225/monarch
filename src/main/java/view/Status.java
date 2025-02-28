package view;

import model.Repository;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for creating a window with actual information regarding the progress.
 */
public class Status {
    private final JFrame frame;
    private final JPanel panel;
    private final JLabel finishedReposLabel;
    private final Map<Repository, JLabel> statusBars;
    private final Map<Repository, Integer> progress = new ConcurrentHashMap<>();
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
        statusBars = new ConcurrentHashMap<>();

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
        progress.put(repo, 0);

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

                label.setText(getProgressString(progress.get(repo)) + " " + ruleName + ": " + repo.getRepositoryName() + ", " + repo.getOwner());
        }
        else {
            System.out.println("No status bar found for " + repo.getRepositoryName());
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
        if (label != null && progress.get(repo) != null) {
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
        frame.setVisible(false);
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
