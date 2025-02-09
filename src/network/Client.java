package network;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Network client for connecting to a game server.
 * Handles socket communication including connection, message sending/receiving,
 * and graceful disconnection.
 */
public class Client {
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    private Socket socket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private boolean isConnected = false;

    /**
     * Establishes connection to the game server
     * @param serverAddress IP/hostname of the server
     * @param port Server port number
     * @throws IOException if connection fails
     */
    public void connect(String serverAddress, int port) throws IOException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddress, port), CONNECTION_TIMEOUT);
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;
            System.out.println("Connected to server at " + serverAddress + ":" + port);
        } catch (SocketTimeoutException e) {
            throw new IOException("Connection timed out: Server not responding");
        }
    }

    /**
     * Sends a message to the server
     * @param message Game state or command string
     * @throws IllegalStateException if not connected
     */
    public void sendMessage(String message) {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to server");
        }
        outputStream.println(message);
    }

    /**
     * Receives a message from the server
     * @return Server response or null if disconnected
     * @throws IOException if network error occurs
     */
    public String receiveMessage() throws IOException {
        if (!isConnected) return null;
        try {
            return inputStream.readLine();
        } catch (SocketException e) {
            handleDisconnection();
            return null;
        }
    }

    /**
     * Closes all network resources
     */
    public void disconnect() throws IOException {
        if (!isConnected) return;

        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
            isConnected = false;
            System.out.println("Disconnected from server");
        } catch (IOException e) {
            System.err.println("Error during disconnection: " + e.getMessage());
        }
    }

    /**
     * @return true if connection is active
     */
    public boolean isConnected() {
        return isConnected && !socket.isClosed();
    }

    private void handleDisconnection() throws IOException {
        isConnected = false;
        System.err.println("Connection lost with server");
        disconnect();
    }
}