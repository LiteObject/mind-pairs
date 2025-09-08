package com.example.mindpairs.game

import com.example.mindpairs.data.UserPreferencesRepository
import com.example.mindpairs.model.Card
import com.example.mindpairs.model.GameDifficulty
import com.example.mindpairs.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameManager(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _gameState = MutableStateFlow(GameState()) // Initial default state
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Nostalgic themed images for older adults
    private val cardImages = listOf(
        "🌹", "🌻", "🌷", "🌺", "🍎", "🍊", "🍇", "🍓",
        "🚗", "📻", "☎️", "📷", "🎵", "🏠", "⭐", "🌙",
        "🎂", "☕", "🍯", "🕊️"
    )

    init {
        coroutineScope.launch {
            val initialDifficulty = userPreferencesRepository.difficultyPreferenceFlow.first()
            // Initialize with the loaded preference. If _gameState is already populated
            // by a preserved GameManager instance, this might restart it.
            // For a typical setup where GameManager is tied to Activity/ViewModel lifecycle,
            // this is fine.
            if (_gameState.value.cards.isEmpty()) { // Load initial game only if not already set up
                 loadInitialGameWithPreference(initialDifficulty)
            }
        }
    }

    // Renamed to avoid confusion with the public startNewGame that users can trigger
    private fun loadInitialGameWithPreference(difficulty: GameDifficulty) {
        val totalCards = difficulty.gridSize.first * difficulty.gridSize.second
        val pairs = totalCards / 2

        val selectedImages = cardImages.take(pairs)
        val cardPairs = selectedImages.flatMap { image ->
            listOf(
                Card(id = selectedImages.indexOf(image) * 2, imageRes = image),
                Card(id = selectedImages.indexOf(image) * 2 + 1, imageRes = image)
            )
        }.shuffled()

        // Preserve best score if it exists from a previous session (via _gameState initial value)
        val currentBestScore = _gameState.value.bestScore 

        _gameState.value = GameState(
            cards = cardPairs,
            difficulty = difficulty,
            bestScore = currentBestScore // Preserve best score across preference loads
        )
        // No need to save preference here as this is just loading it.
    }

    fun startNewGame(difficulty: GameDifficulty) {
        val totalCards = difficulty.gridSize.first * difficulty.gridSize.second
        val pairs = totalCards / 2

        val selectedImages = cardImages.take(pairs)
        val cardPairs = selectedImages.flatMap { image ->
            listOf(
                Card(id = selectedImages.indexOf(image) * 2, imageRes = image),
                Card(id = selectedImages.indexOf(image) * 2 + 1, imageRes = image)
            )
        }.shuffled()
        
        // Preserve best score from the current state
        val currentBestScore = _gameState.value.bestScore

        _gameState.value = GameState(
            cards = cardPairs,
            difficulty = difficulty,
            bestScore = currentBestScore // Ensure bestScore isn't reset on difficulty change
        )

        coroutineScope.launch {
            userPreferencesRepository.saveDifficultyPreference(difficulty)
        }
    }

    fun flipCard(cardId: Int) {
        val currentState = _gameState.value
        if (currentState.flippedCards.size >= 2 || currentState.isGameComplete) return

        val cardIndex = currentState.cards.indexOfFirst { it.id == cardId }
        if (cardIndex == -1) return

        val card = currentState.cards[cardIndex]
        if (card.isFlipped || card.isMatched) return

        val updatedCards = currentState.cards.toMutableList()
        updatedCards[cardIndex] = card.copy(isFlipped = true)

        val newFlippedCards = currentState.flippedCards + card.copy(isFlipped = true)

        _gameState.value = currentState.copy(
            cards = updatedCards,
            flippedCards = newFlippedCards
        )

        if (newFlippedCards.size == 2) {
            checkForMatch()
        }
    }

    private fun checkForMatch() {
        val currentState = _gameState.value
        val flippedCards = currentState.flippedCards

        if (flippedCards.size != 2) return

        val isMatch = flippedCards[0].imageRes == flippedCards[1].imageRes
        val newMoves = currentState.moves + 1

        if (isMatch) {
            val updatedCards = currentState.cards.map {
                if (flippedCards.any { fc -> fc.id == it.id }) it.copy(isMatched = true, isFlipped = true) else it
            }
            val newMatchedPairs = currentState.matchedPairs + 1
            val totalPairs = currentState.difficulty.gridSize.first * currentState.difficulty.gridSize.second / 2
            val isGameComplete = newMatchedPairs == totalPairs
            val newBestScore = if (isGameComplete && newMoves < currentState.bestScore) newMoves else currentState.bestScore

            _gameState.value = currentState.copy(
                cards = updatedCards,
                flippedCards = emptyList(),
                matchedPairs = newMatchedPairs,
                moves = newMoves,
                isGameComplete = isGameComplete,
                bestScore = newBestScore
            )
        } else {
            coroutineScope.launch {
                delay(1000)
                val updatedCards = currentState.cards.map {
                    if (flippedCards.any { fc -> fc.id == it.id }) it.copy(isFlipped = false) else it
                }
                _gameState.value = _gameState.value.copy(
                    cards = updatedCards,
                    flippedCards = emptyList(),
                    moves = newMoves
                )
            }
        }
    }

    fun resetGame() {
        startNewGame(_gameState.value.difficulty)
    }
}
