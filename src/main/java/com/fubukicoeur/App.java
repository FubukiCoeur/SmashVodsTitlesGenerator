package com.fubukicoeur;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class App {

    private static final String TOKEN_FILE = "token.txt";
    private static final String IMAGE_PATH = "assets/guide.png"; 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Start.gg Vod Titles Generator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 500);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // Menu bar
            JMenuBar menuBar = new JMenuBar();
            JMenu settingsMenu = new JMenu("Settings");
            JMenuItem tokenItem = new JMenuItem("Register API Token");
            settingsMenu.add(tokenItem);
            menuBar.add(settingsMenu);
            frame.setJMenuBar(menuBar);

            // Center panel with vertical BoxLayout
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Image label (bigger)
            JLabel imageLabel = new JLabel();
            try {
                ImageIcon icon = new ImageIcon(IMAGE_PATH);
                Image scaled = icon.getImage().getScaledInstance(500, -1, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
            } catch (Exception ex) {
                imageLabel.setText("Image not found: " + IMAGE_PATH);
            }
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(imageLabel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));

            // Event Slug label
            JLabel slugLabel = new JLabel("Event Slug:");
            slugLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(slugLabel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            // Event Slug input
            JTextField slugField = new JTextField(30);
            slugField.setMaximumSize(new Dimension(400, slugField.getPreferredSize().height));
            slugField.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(slugField);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));

            // Event label
            JLabel eventLabel = new JLabel("Event name (use a reduced version for YouTube 100 characters limit):");
            eventLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(eventLabel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            // Event name input
            JTextField eventField = new JTextField(30);
            eventField.setMaximumSize(new Dimension(400, eventField.getPreferredSize().height));
            eventField.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(eventField);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));

            // Progress label (initially hidden)
            JLabel progressLabel = new JLabel(" ");
            progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(progressLabel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Generate button
            JButton generateButton = new JButton("Generate");
            generateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(generateButton);

            // Load saved token
            String[] savedToken = { loadToken() };

            // Token menu action
            tokenItem.addActionListener(_ -> {
                String input = JOptionPane.showInputDialog(frame, "Enter your API Token:",
                        savedToken[0] != null ? savedToken[0] : "");
                if (input != null && !input.trim().isEmpty()) {
                    savedToken[0] = input.trim();
                    saveToken(savedToken[0]);
                }
            });

            // Generate button action
            generateButton.addActionListener((ActionEvent e) -> {
                String slug = slugField.getText().trim();
                if (slug.isEmpty() || savedToken[0] == null || savedToken[0].isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Invalid Slug or Token.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String eventName = eventField.getText().trim();
                if (eventName.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Event name cannot be empty.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                generateButton.setEnabled(false);
                generateButton.setText("Generating...");
                progressLabel.setText("Initializing...");

                new Thread(() -> {
                    try {
                        // Create a progress callback to update the UI
                        ProgressCallback progressCallback = (currentPage, totalPages, eventSlug) -> {
                            SwingUtilities.invokeLater(() -> {
                                progressLabel.setText("Fetching page " + currentPage + "/" + totalPages + " of sets for event: " + eventSlug);
                            });
                        };

                        ApiCalls api = new ApiCalls(savedToken[0], slug);
                        List<MatchInfo> matches = api.getAllStreamedSetDetails(progressCallback);

                        SwingUtilities.invokeLater(() -> {
                            progressLabel.setText("Writing to file...");
                        });

                        // Write matches to file manually
                        writeMatchesToFile(matches, eventName, "sets.txt");

                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "sets.txt file generated successfully in app folder", "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            generateButton.setEnabled(true);
                            generateButton.setText("Generate");
                            progressLabel.setText(" "); // Clear progress text
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "Error : " + ex.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            generateButton.setEnabled(true);
                            generateButton.setText("Generate");
                            progressLabel.setText(" "); // Clear progress text
                        });
                    }
                }).start();
            });

            frame.add(centerPanel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
    
    /**
     * Writes match information to a text file
     * @param matches List of MatchInfo objects to write
     * @param eventName The name of the event to prepend to each match
     * @param filename The name of the file to write to
     * @throws IOException if there's an error writing to the file
     */
    private static void writeMatchesToFile(List<MatchInfo> matches, String eventName, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (MatchInfo match : matches) {
                writer.write(eventName + " - " + match.toString());
                writer.newLine();
            }
        }
    }

    private static void saveToken(String token) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TOKEN_FILE))) {
            writer.write(token);
        } catch (IOException e) {
            System.err.println("Error saving token: " + e.getMessage());
        }
    }

    private static String loadToken() {
        File file = new File(TOKEN_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine();
            } catch (IOException e) {
                System.err.println("Error loading token: " + e.getMessage());
            }
        }
        return null;
    }

    // Functional interface for progress callback
    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(int currentPage, int totalPages, String eventSlug);
    }
}