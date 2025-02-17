package game;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

/**
 * Main game panel handling Pac-Man gameplay mechanics, rendering, and network synchronization.
 * Implements core game logic including movement, collision detection, and state management.
 */
public class PacMan extends JPanel implements ActionListener, KeyListener {

    /**
     * Represents a game entity (wall, ghost, food, or player)
     */
    class Block {
        int x, y, width, height;
        Image image;

        int startX, startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
            updateVelocity();
        }

        /**
         * Updates movement direction and handles wall collisions
         */
        void updateDirection(char newDirection) {
            char prevDirection = this.direction;
            this.direction = newDirection;
            updateVelocity();

            // Test movement and revert if collision occurs
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                    break;
                }
            }
        }

        /**
         * Sets velocity based on current direction
         */
        void updateVelocity() {
            velocityX = switch (direction) {
                case 'L' -> -tileSize/4;
                case 'R' -> tileSize/4;
                default -> 0;
            };

            velocityY = switch (direction) {
                case 'U' -> -tileSize/4;
                case 'D' -> tileSize/4;
                default -> 0;
            };
        }

        void reset() {
            x = startX;
            y = startY;
            velocityX = 0;
            velocityY = 0;
        }
    }

    // region Game Configuration Constants
    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;
    private static final int INITIAL_LIVES = 3;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    // region Game State
    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;
    Map<String, Block> otherPlayers = new HashMap<>();
    int score = 0;
    int lives = INITIAL_LIVES;
    boolean gameOver = false;

    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();

    GameNetwork gameNetwork;
    Timer gameLoop;

    // region Initialization
    public PacMan(GameNetwork gameNetwork) {
        this.gameNetwork = gameNetwork;

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load images
        wallImage = new ImageIcon(getClass().getResource("/resources/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/resources/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/resources/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/resources/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/resources/redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("/resources/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/resources/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/resources/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/resources/pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        gameLoop = new Timer(50, this); // 20 FPS
        gameLoop.start();
    }


    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                if (tileMapChar == 'X') {
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                } else if (tileMapChar == 'b') {
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'o') {
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'p') {
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'r') {
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                } else if (tileMapChar == ' ') {
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }

        // Notify the server to reset eaten foods (via a special message)
        if (gameNetwork.isConnected) {
            gameNetwork.sendGameState("RESET_FOODS"); // Custom signal
        }


    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block other : otherPlayers.values()) {
            g.drawImage(other.image, other.x, other.y, other.width, other.height, null);
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + score, tileSize / 2, tileSize / 2);
        } else {
            g.drawString("x" + lives + " Score: " + score, tileSize / 2, tileSize / 2);
        }
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    String gameState = serializeGameState();
                    gameNetwork.sendGameState(gameState);
                    return;
                }
                resetPositions();
            }

            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String receivedState = gameNetwork.getLatestGameState();
            if (receivedState != null) {
                deserializeGameState(receivedState);
                gameNetwork.latestGameState = null;
            }
            move();
            if (gameNetwork.isConnected) {
                String gameState = serializeGameState();
                gameNetwork.sendGameState(gameState);
            }
            repaint();
            if (gameOver) {
                gameLoop.stop();
            }
        } catch (Exception ex) {
            System.err.println("Error in game loop: " + ex.getMessage());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();

            // Send reset signal and force a state update
            gameNetwork.sendGameState("RESET_FOODS");
            String gameState = serializeGameState();
            gameNetwork.sendGameState(gameState);
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        } else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        } else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        } else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }


        // Send the updated game state to the server
        String gameState = serializeGameState();
        gameNetwork.sendGameState(gameState);
    }

    private String serializeGameState() {
        StringBuilder state = new StringBuilder();
        state.append(gameNetwork.getPlayerId()).append(",")
                .append(pacman.x).append(",")
                .append(pacman.y).append(",")
                .append(pacman.direction).append(",")
                .append(score).append(",")
                .append(lives).append(",")
                .append(gameOver);

        // Append eaten food positions
        for (Block food : foods) {
            if (collision(pacman, food)) {
                state.append(",FOOD:").append(food.x).append(",").append(food.y);
            }
        }
        return state.toString();
    }

    private void deserializeGameState(String gameState) {
        if (gameState == null || gameState.isEmpty()) return;

        try {
            // Split into player states and global foods
            String[] parts = gameState.split("#", 2);
            String playerStatesStr = parts[0];
            String globalFoodsStr = (parts.length > 1) ? parts[1] : "";

            // Process player states
            String[] playerStates = playerStatesStr.split(";");
            for (String state : playerStates) {
                if (state.isEmpty()) continue;

                String[] playerData = state.split(",");
                if (playerData.length < 7) continue; // Ensure valid format

                String playerId = playerData[0];
                if (playerId.equals(/*this.*/gameNetwork.getPlayerId())) {
                    continue; // Skip own state
                }

                Block other = otherPlayers.get(playerId);
                if (other == null) {
                    Image image = pacmanRightImage;
                    other = new Block(image,
                            Integer.parseInt(playerData[1]),
                            Integer.parseInt(playerData[2]),
                            tileSize, tileSize);
                    otherPlayers.put(playerId, other);
                }
                // Update other player's state
                other.x = Integer.parseInt(playerData[1]);
                other.y = Integer.parseInt(playerData[2]);
                other.direction = playerData[3].charAt(0);

                // Check if this player is game over and show a message
                boolean isOtherGameOver = Boolean.parseBoolean(playerData[6]);
                if (isOtherGameOver) {
                    JOptionPane.showMessageDialog(this, "Player " + playerId + " lost!");
                }

                switch (other.direction) {
                    case 'U' -> other.image = pacmanUpImage;
                    case 'D' -> other.image = pacmanDownImage;
                    case 'L' -> other.image = pacmanLeftImage;
                    case 'R' -> other.image = pacmanRightImage;
                }
            }


        // Update global foods
        Set<String> globalEatenFoods = new HashSet<>(Arrays.asList(globalFoodsStr.split(";")));
       /* if (!globalFoodsStr.isEmpty()) {
            globalEatenFoods.addAll(Arrays.asList(globalFoodsStr.split(",")));
        }*/

        Iterator<Block> foodIterator = foods.iterator();
        while (foodIterator.hasNext()) {
            Block food = foodIterator.next();
            String foodPos = food.x + "," + food.y;
            if (globalEatenFoods.contains(foodPos)) {
                foodIterator.remove();
            }
        }

        /*// Check for game over in other players' states
        for (String state : playerStates) {
            String[] playerData = state.split(",");
            if (playerData.length >= 7 && Boolean.parseBoolean(playerData[6])) {
                // Another player is game over
                gameOver = true;
                JOptionPane.showMessageDialog(this, "Player " + playerData[0] + " lost! You win!");
                break;
            }
        }*/
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Malformed game state: " + gameState);
        }
    }
}