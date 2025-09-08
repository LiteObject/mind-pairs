# Mind Pairs - Memory Match-Up Game

A cognitive exercise Android app designed specifically for older adults to improve memory and cognitive function through an engaging card-matching game.

## Features

### Game Mechanics
- **Memory Training**: Match pairs of cards by flipping them over
- **Three Difficulty Levels**: 
  - Easy (6 cards) - Perfect for beginners
  - Medium (16 cards) - Standard challenge
  - Hard (20 cards) - Advanced difficulty
- **Score Tracking**: Move counter and best score persistence
- **Instant Restart**: Quick game reset functionality

### Accessibility for Older Adults
- **Large, Clear Visuals**: High-contrast colors and extra-large text (18sp+)
- **Nostalgic Themes**: Familiar imagery (flowers, fruits, vintage items)
- **Simple Interface**: Minimal, uncluttered design
- **Gentle Animations**: 1-second card reveal delay for easier viewing
- **Touch-Friendly**: Large tap targets for better usability

## Technical Details

### Built With
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern Android UI toolkit
- **Material Design 3** - UI components and theming
- **Android Architecture Components** - MVVM pattern

### Requirements
- **Minimum SDK**: Android 10.0 (API level 30)
- **Target SDK**: Android 14 (API level 36)
- **Android Studio**: Flamingo or later

## Getting Started

### Prerequisites
- Android Studio installed
- Android device or emulator running Android 10.0+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/LiteObject/mind-pairs.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Create an Android Virtual Device (AVD) or connect a physical device

5. Run the app using the green play button

### Setting up an Emulator
1. Go to **Tools → AVD Manager**
2. Click **Create Virtual Device**
3. Select **Pixel 7** or similar device
4. Choose **API Level 34** (Android 14)
5. Click **Finish** and start the emulator

## How to Play

1. **Choose Difficulty**: Select Easy, Medium, or Hard from the top chips
2. **Flip Cards**: Tap any two cards to reveal them
3. **Find Matches**: If cards match, they stay face-up
4. **Continue**: Keep flipping until all pairs are found
5. **Win**: Complete the game in the fewest moves possible!

## Game Statistics

- **Moves Counter**: Tracks the number of card flips
- **Pairs Found**: Shows progress through the current game
- **Best Score**: Saves your lowest move count for each difficulty

## Project Structure

```
app/src/main/java/com/example/mindpairs/
├── model/
│   ├── Card.kt              # Card data model
│   └── GameState.kt         # Game state and difficulty levels
├── game/
│   └── GameManager.kt       # Core game logic and state management
├── ui/
│   ├── components/
│   │   └── MemoryCard.kt    # Animated card component
│   ├── screens/
│   │   └── GameScreen.kt    # Main game interface
│   └── theme/
│       ├── Color.kt         # Accessible color palette
│       ├── Theme.kt         # Material 3 theme configuration
│       └── Type.kt          # Large, readable typography
└── MainActivity.kt          # App entry point
```

## Design Philosophy

This app is specifically designed for older adults with focus on:
- **Cognitive Health**: Exercises working memory and pattern recognition
- **Accessibility**: Large text, high contrast, simple navigation
- **Engagement**: Nostalgic imagery and positive reinforcement
- **Social Interaction**: Encourages group play and discussion

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Made with care for cognitive wellness and healthy aging**
