package game;

import network.Client;

import java.io.IOException;

/**
 * Handles network communication between game client and server.
 * Manages connection lifecycle and message processing.
 * Supports 4 message types:
 * 1. PLAYER_STATE - Player position/direction updates
 * 2. FOOD_EATEN - Food consumption updates
 * 3. GAME_OVER - Game termination status
 * 4. RESET_FOODS - Food positions reset
 */
public class GameNetwork {
    private Client client;
    public boolean isConnected = false;
    private String playerId;
    public volatile String latestGameState;

    /**
     * Initializes network components
     */
    public GameNetwork() {
        client = new Client();
    }

    /**
     * Establishes connection to game server
     * @param address Server IP/hostname
     * @param port Server port number
     * @throws IOException if connection fails
     */
    public void connectToServer(String address, int port) throws IOException {
        try {
            client.connect(address, port);
            handleServerHandshake();
            startMessageReceiver();
        } catch (IOException e) {
            handleConnectionError(e);
            throw e;
        }
    }

    /**
     * Processes initial server handshake message
     */
    private void handleServerHandshake() throws IOException {
        String handshake = client.receiveMessage();
        if (handshake != null) {
            String[] parts = handshake.split("#", 2);
            playerId = parts[0];
            latestGameState = (parts.length > 1) ? "#" + parts[1] : "";
        }
        isConnected = true;
        System.out.println("Connected to server. Player ID: " + playerId);
    }

    /**
     * Starts background thread for receiving server messages
     */
    private void startMessageReceiver() {
        new Thread(this::receiveMessages).start();
    }

    /**
     * Continuous message receiving loop
     */
    private void receiveMessages() {
        try {
            while (isConnected) {
                String message = client.receiveMessage();
                if (message == null) {
                    handleDisconnection();
                    break;
                }
                latestGameState = message;
            }
        } catch (IOException e) {
            handleNetworkError(e);
        }
    }

    /**
     * @return Unique player identifier assigned by server
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * @return Last received game state from server
     */
    public String getLatestGameState() {
        return latestGameState;
    }

    /**
     * Sends game state update to server
     * @param state Serialized game state string
     */
    public void sendGameState(String state) {
        if (!isConnected) {
            System.err.println("Send failed: Not connected to server");
            return;
        }
        client.sendMessage(state);
    }

    /**
     * Closes network connection
     */
    public void disconnect() throws IOException {
        if (isConnected) {
            client.disconnect();
            isConnected = false;
            System.out.println("Disconnected from server");
        }
    }

    private void handleConnectionError(IOException e) {
        System.err.println("Connection error: " + e.getMessage());
        isConnected = false;
    }

    private void handleNetworkError(IOException e) {
        System.err.println("Network error: " + e.getMessage());
        isConnected = false;
    }

    private void handleDisconnection() {
        System.err.println("Server disconnected");
        isConnected = false;
    }
}