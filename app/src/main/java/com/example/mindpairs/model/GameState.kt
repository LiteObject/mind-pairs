package com.example.mindpairs.model

enum class GameDifficulty(val gridSize: Pair<Int, Int>, val displayName: String) {
    EASY(Pair(4, 2), "Easy (8 cards)"), // Updated to 4 columns, 3 rows
    MEDIUM(Pair(4, 3), "Medium (12 cards)"),
    HARD(Pair(4, 4), "Hard (16 cards)")
}

data class GameState(
    val cards: List<Card> = emptyList(),
    val flippedCards: List<Card> = emptyList(),
    val matchedPairs: Int = 0,
    val moves: Int = 0,
    val isGameComplete: Boolean = false,
    val difficulty: GameDifficulty = GameDifficulty.MEDIUM,
    val bestScore: Int = Int.MAX_VALUE
)
