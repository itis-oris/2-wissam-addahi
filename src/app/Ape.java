package app;

import game.GameNetwork;
import ui.GameFrame;
import java.io.IOException;

/**
 * Main client application entry point for Pac-Man multiplayer game.
 * Handles network initialization and GUI startup.
 */
public class Ape {
    // Network configuration constants
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;

    public static void main(String[] args) {
        try {
            // 1. Initialize network communication handler
            GameNetwork gameNetwork = new GameNetwork();

            // 2. Establish connection to game server
            connectToServer(gameNetwork);

            // 3. Launch game window with network capabilities
            new GameFrame("Pac-Man Multiplayer", gameNetwork);

        } catch (Exception e) {
            System.err.println("Fatal initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Establishes connection to the game server
     * @param network Game network controller instance
     * @throws IOException if connection fails due to network issues
     */
    private static void connectToServer(GameNetwork network) throws IOException {
        try {
            network.connectToServer(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connection established with game server at "
                    + SERVER_ADDRESS + ":" + SERVER_PORT);
        } catch (IOException ex) {
            throw new IOException("Server connection failed: " + ex.getMessage()
                    + " - Check if server is running and accessible");
        }
    }
}