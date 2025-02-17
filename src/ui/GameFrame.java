package ui;

import game.GameNetwork;
import game.PacMan;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Main game window container managing the GUI layout and lifecycle.
 * Handles connection cleanup when closed.
 */
public class GameFrame extends JFrame {
    // Game board configuration
    private static final int ROWS = 21;         // Vertical tile count
    private static final int COLS = 19;         // Horizontal tile count
    private static final int TILE_SIZE = 32;    // Pixels per game tile

    /**
     * Creates game window with networking capabilities
     * @param title Window title text
     * @param network Game network controller instance
     */
    public GameFrame(String title, GameNetwork network) {
        super(title);
        configureFrame();
        initGamePanel(network);
        setupWindowListener(network);
        setVisible(true); // Display after full initialization
    }

    /**
     * Configures window properties and dimensions
     */
    private void configureFrame() {
        // Set fixed size based on game grid dimensions
        setSize(COLS * TILE_SIZE, ROWS * TILE_SIZE);
        setResizable(false); // Prevent window resizing
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Initializes and adds the game panel component
     * @param network Active network connection
     */
    private void initGamePanel(GameNetwork network) {
        PacMan gamePanel = new PacMan(network);
        add(gamePanel); // Add to JFrame's content pane
        pack(); // Adjust window to preferred component sizes

        // Ensure keyboard focus starts with game panel
        gamePanel.requestFocusInWindow();
    }

    /**
     * Sets up window closing handler for clean disconnect
     */
    private void setupWindowListener(GameNetwork network) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleGracefulShutdown(network);
            }
        });
    }

    /**
     * Performs network cleanup in separate thread to avoid GUI freeze
     */
    private void handleGracefulShutdown(GameNetwork network) {
        new Thread(() -> {
            try {
                if (network.isConnected) {
                    network.disconnect();
                    System.out.println("Network connection closed");
                }
            } catch (IOException ex) {
                System.err.println("Disconnect error: " + ex.getMessage());
            }
            System.exit(0); // Terminate application
        }).start();
    }
}