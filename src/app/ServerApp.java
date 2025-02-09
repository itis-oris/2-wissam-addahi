package app;

import network.Server;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Server application entry point for Pac-Man multiplayer game.
 * Handles server initialization and client connections.
 */

public class ServerApp {
    // Network configuration constants
    private static final int SERVER_PORT = 1234;

    public static void main(String[] args) {
        try {
            // Initialize game server instance
            Server server = new Server();

            // Start listening for client connections
            server.start(SERVER_PORT);

            // Display server network information
            printServerInfo();

            // Keep server running indefinitely
            while(true) {
                Thread.sleep(1000); // Prevent immediate exit
            }
        } catch (IOException e) {
            handleServerError("Failed to start server", e);
        } catch (InterruptedException e) {
            handleServerError("Server interrupted", e);
        }
    }

    /**
     * Displays server IP and status information
     */
    private static void printServerInfo() throws IOException {
        String localIP = InetAddress.getLocalHost().getHostAddress();
        System.out.println("╔══════════════════════════════╗");
        System.out.println("║ PAC-MAN SERVER               ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.printf("║ IP: %-15s          ║\n", localIP);
        System.out.printf("║ Port: %-14d         ║\n", SERVER_PORT);
        System.out.println("╚══════════════════════════════╝");
        System.out.println("Waiting for players to connect...");
    }

    /**
     * Handles server errors with consistent formatting
     */
    private static void handleServerError(String message, Exception e) {
        System.err.println("\n! ERROR: " + message);
        System.err.println("! Reason: " + e.getMessage());
        System.err.println("! Server shutting down...");
        e.printStackTrace();
        System.exit(1);
    }

}