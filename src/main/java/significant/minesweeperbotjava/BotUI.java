package significant.minesweeperbotjava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author tompp
 */
public class BotUI extends JFrame {
    private JComboBox<String> stageSelector;
    private String selectedResolution = "1080p";
    private JComboBox<String> resolutionSelector;
    private JButton startButton;
    private JButton stopButton;
    private JButton quitButton;
    private JLabel statusLabel;
    private volatile boolean stopBot = false;
    private BufferedImage[] templates;
    
    // Stage configurations: x, y, width, height, rows, cols, cellsize
    private final int[][] stageConfigs1080p = {
        {780, 455, 360, 360, 9, 9, 40},     // Stage 1
        {719, 415, 480, 440, 11, 12, 40},   // Stage 2
        {659, 375, 600, 520, 13, 15, 40},   // Stage 3
        {599, 355, 720, 560, 14, 18, 40},   // Stage 4
        {560, 315, 800, 640, 16, 20, 40}    // Stage 5
    };
    
    private final int[][] stageConfigs2K = {
        {1064, 546, 432, 432, 9, 9, 48},     // Stage 1
        {991, 498, 576, 528, 11, 12, 48},    // Stage 2
        {920, 450, 720, 624, 13, 15, 48},    // Stage 3
        {847, 427, 864, 672, 14, 18, 48},    // Stage 4
        {799, 379, 960, 768, 16, 20, 48}     // Stage 5
    };
    
    private final int[][] stageConfigs4K = {
        {1596, 819, 648, 648, 9, 9, 72},     // Stage 1
        {1486, 747, 864, 792, 11, 12, 72},   // Stage 2
        {1380, 675, 1080, 936, 13, 15, 72},  // Stage 3
        {1270, 640, 1296, 1008, 14, 18, 72}, // Stage 4
        {1198, 568, 1440, 1152, 16, 20, 72}  // Stage 5
    };
    
    public BotUI() {
        setTitle("Minesweeper Bot for Spleen Sweeper in Dota 2");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 1));
        
        // Resolution selector
        resolutionSelector = new JComboBox<>(new String[]{"1080p", "2K", "4K"});
        resolutionSelector.setRenderer(new DisabledItemRenderer());
        resolutionSelector.addActionListener(e -> {
            String selected = (String) resolutionSelector.getSelectedItem();
            if ("4K".equals(selected)) {
                JOptionPane.showMessageDialog(this, "4K resolution is not available yet. We are working dilligently to get this working.", "Unavailable option", JOptionPane.WARNING_MESSAGE);
                resolutionSelector.setSelectedItem("2K");
            } else {
                selectedResolution = selected;
            }
        });
        add(new JLabel("Select Resolution:"));
        add(resolutionSelector);
        
        stageSelector = new JComboBox<>(new String[]{"Stage 1", "Stage 2", "Stage 3", "Stage 4", "Stage 5"});
        add(new JLabel("Select Stage:"));
        add(stageSelector);
        
        startButton = new JButton("Start");
        add(startButton);
        
        stopButton = new JButton("Stop");
        add(stopButton);
        
        quitButton = new JButton("Quit");
        add(quitButton);
        
        statusLabel = new JLabel("Status: waiting for user action", SwingConstants.CENTER);
        add(statusLabel);
        
        // Add action listeners
        startButton.addActionListener(new StartButtonListener());
        stopButton.addActionListener(e -> {
            stopBot = true;
            statusLabel.setText("Status: Bot stopped by user.");
        });
        quitButton.addActionListener(e -> System.exit(0));
    }
    
    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Reset the stop flag
            stopBot = false;
            // Get selected stage
            int selectedStage = stageSelector.getSelectedIndex() + 1;
            
            if (templates == null) {
                templates = BotUI.this.loadTemplates(selectedResolution);
            }
            
            // Update status
            statusLabel.setText("Running Stage " + selectedStage + "...");
            
            // Start the bot for the selected stage
            
            new Thread(() -> {
                boolean stageComplete = runBot(selectedStage);
                SwingUtilities.invokeLater(() -> {
                    if (stageComplete) {
                        statusLabel.setText("Stage " + selectedStage + "Complete!");
                    } else {
                        statusLabel.setText("No more moves! Back to UI.");
                    }
                });
            }).start();
        }
    }
    
    private BufferedImage[] loadTemplates(String resolution) {
    templates = new BufferedImage[9];
    String templateFolder = "/templates/" + resolution + "/";

    for (int i = 0; i <= 8; i++) {
        String fileName = switch (i) {
            case 7 -> "flagged.png";
            case 8 -> "covered.png";
            default -> i + ".png"; // 0-6 for numbers
        };

        try (InputStream is = getClass().getResourceAsStream(templateFolder + fileName)) {
            if (is == null) {
                System.err.println("Failed to find template: " + templateFolder + fileName);
                continue;
            }
            templates[i] = ImageIO.read(is);
            System.out.println("Loaded template: " + fileName);
        } catch (IOException e) {
            System.err.println("Error loading template: " + templateFolder + fileName);
        }
    }
    return templates;
}
    
    private class DisabledItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if ("4K".equals(value)) {
                component.setEnabled(false);
                component.setForeground(Color.GRAY);
            } else {
                component.setEnabled(true);
                component.setForeground(Color.BLACK);
            }
            
            return component;
        }
    }
    private boolean runBot(int stage) {
        try {
            
            int [][] stageConfigs;
            
            stageConfigs = switch (selectedResolution) {
                case "2K" -> stageConfigs2K;
                case "4K" -> stageConfigs4K;
                default -> stageConfigs1080p; // 1080 p
            };

            // Extract stage-specific configurations
            int x = stageConfigs[stage - 1][0];
            int y = stageConfigs[stage - 1][1];
            int width = stageConfigs[stage - 1][2];
            int height = stageConfigs[stage - 1][3];
            int rows = stageConfigs[stage - 1][4];
            int cols = stageConfigs[stage - 1][5];
            int cellSize = stageConfigs[stage - 1][6];

            // Initialize screen capture
            ScreenCapture screenCapture = new ScreenCapture(x, y, width, height);
            
            int centerX = x + (width / 2);
            int centerY = y + (height / 2);
            ActionExecutor actionExecutor = new ActionExecutor();
            actionExecutor.activateWindow(centerX, centerY);

            boolean hasMoves = true;

            while (hasMoves) {
                if (stopBot) {
                    System.out.println("Bot stopped by user.");
                    return false; // Exit gracefully
                }
                // Capture and process the board
                BufferedImage boardImage = screenCapture.capture();
                BoardAnalyzer analyzer = new BoardAnalyzer(boardImage, rows, cols, cellSize, x, y, selectedResolution, templates);
                analyzer.findSafeMovesAndFlags();
                actionExecutor.moveMouse(511, 767);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    System.err.println("Sleep interrupted: " + e.getMessage());
                }
                

                // Check for pending moves
                hasMoves = analyzer.hasPendingMoves();
                if (!hasMoves) break;

                // Optional delay for smooth performance
                Thread.sleep(100);
            }

            return true; // Stage complete
        } catch (AWTException | InterruptedException ex) {
            return false; // No more moves or an error occurred
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BotUI botUI = new BotUI();
            botUI.setVisible(true);
        });
    }
}
