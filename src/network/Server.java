package network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Game server implementation handling client connections and game state synchronization.
 * Supports 4 message types:
 * 1. RESET_FOODS - Reset all food positions
 * 2. FOOD:x,y - Mark food as eaten
 * 3. PLAYER_STATE - Player position/direction updates
 * 4. INIT - Initial connection handshake
 */
public class Server {
    // Core server components
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private Map<String, String> playerStates = new ConcurrentHashMap<>();
    private Set<String> eatenFoods = new ConcurrentHashSet<>(); // Track eaten food by position "x,y"

    /**
     * Starts the server on specified port
     * @param port Network port to listen on
     */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
                System.out.println("Server shutdown complete");
            } catch (IOException e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));

        // Client connection acceptor thread
        new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler client = new ClientHandler(clientSocket, this);
                    synchronized (clients) {
                        clients.add(client);
                    }
                    new Thread(client).start();
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Connection error: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Updates game state from client messages
     * @param playerId Unique client identifier
     * @param state Message payload containing game data
     */
    public synchronized void updatePlayerState(String playerId, String state) {
        // Handle special commands
        if (state.startsWith("RESET_FOODS")) {
            eatenFoods.clear();
        } else if (state.startsWith("FOOD:")) {
            String foodPos = state.substring(5);
            eatenFoods.add(foodPos);
        } else {
            playerStates.put(playerId, state);
        }
        broadcastGameState();
    }

    /**
     * Removes disconnected player from game state
     */
    public synchronized void removePlayer(String playerId) {
        playerStates.remove(playerId);
        broadcastGameState();
    }

    /**
     * Broadcasts combined game state to all connected clients
     */
    private synchronized void broadcastGameState() {
        String statePayload = String.join(";", playerStates.values())
                + "#" + String.join(";", eatenFoods);

        // Thread-safe iteration through clients
        synchronized (clients) {
            clients.removeIf(client -> {
                if (!client.isActive()) return true;
                client.sendGameState(statePayload);
                return false;
            });
        }
    }

    /**
     * Graceful server shutdown sequence
     */
    public void shutdown() throws IOException {
        System.out.println("Initiating server shutdown...");
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.disconnect();
            }
            clients.clear();
        }
        serverSocket.close();
    }

    /**
     * Handles individual client connections
     */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Server server;
        private PrintWriter out;
        private BufferedReader in;
        private String playerId;
        private boolean isConnected = true;

        public ClientHandler(Socket socket, Server server) {
            this.clientSocket = socket;
            this.server = server;
        }

        public void run() {
            try (InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
                 BufferedReader br = new BufferedReader(isr);
                 PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true)) {

                this.out = pw;
                this.in = br;

                // Initialize client connection
                initializeClient();
                processClientMessages();

            } catch (SocketException e) {
                System.out.println("Client " + playerId + " disconnected: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Client error: " + e.getMessage());
            } finally {
                cleanupClient();
            }
        }

        private void initializeClient() throws IOException {
            // Generate unique player ID
            playerId = UUID.randomUUID().toString();

            // Send initial game state
            String initialData = playerId + "#" + String.join(",", server.eatenFoods);
            out.println(initialData);

            System.out.println("Player connected: " + playerId);
        }

        private void processClientMessages() throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                server.updatePlayerState(playerId, message);
            }
        }

        private void cleanupClient() {
            isConnected = false;
            try {
                if (playerId != null) {
                    server.removePlayer(playerId);
                }
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Cleanup error: " + e.getMessage());
            }
            System.out.println("Player disconnected: " + playerId);
        }

        public void sendGameState(String state) {
            if (isConnected) {
                out.println(state);
            }
        }

        public void disconnect() throws IOException {
            try {
                isConnected = false;
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Disconnect error: " + e.getMessage());
            }
        }

        public boolean isActive() {
            return !clientSocket.isClosed() && isConnected;
        }
    }
}