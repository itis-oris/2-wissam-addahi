```markdown
# Pac-Man Multiplayer 

## Features
- 🕹️ Classic Pac-Man gameplay with multiplayer support
- 🌐 Client-server architecture using Java sockets
- 🎨 Custom GUI with Swing and custom rendering
- 🔄 Real-time synchronization of game states
- 🚦 Multi-threaded network handling

## Project Structure
```
```
project/
├── src/
│   ├── main/java/
│   │   ├── app/          # Application entry points
│   │   ├── game/         # Game logic and network handling
│   │   ├── network/      # Socket implementation
│   │   └── ui/           # GUI components
│   └── resources/        # Game assets
└── README.md
```

## Game Protocol
The custom protocol supports 4 message types:
1. `PLAYER_STATE` - Player position/direction (format: `playerId,x,y,direction,score,lives,gameOver`)
2. `FOOD_EATEN` - Food consumption (format: `FOOD:x,y`)
3. `GAME_OVER` - Game termination
4. `RESET_FOODS` - Reset all food positions

## Controls
- Arrow keys: Move Pac-Man
- R: Restart game (when game over)
- ESC: Exit game

## Implementation Details
- **Network Architecture**:  
  Uses client-server model with TCP sockets
- **Thread Management**:
    - Dedicated thread for network I/O
    - Swing Event Dispatch Thread for GUI
- **Synchronization**:
    - Game state updates every 50ms
    - Concurrent collections for thread safety
- **Error Handling**:
    - Graceful disconnection handling
    - Network timeout detection

## Developers
- [Wissam Addahi](https://github.com/wimhad)
## Contacts 
- [Telegram](https://t.me/wimhad)
- [Instagram](https://instagram.com/wimhad)

