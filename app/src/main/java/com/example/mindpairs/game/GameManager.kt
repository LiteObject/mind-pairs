package com.example.mindpairs.game

import com.example.mindpairs.data.UserPreferencesRepository
import com.example.mindpairs.model.Card
import com.example.mindpairs.model.GameDifficulty
import com.example.mindpairs.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GameManager(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val coroutineScope: CoroutineScope // Added coroutineScope to constructor
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Removed internal gameScope, will use injected coroutineScope

    private val cardImages = listOf(
        "🌹", "🌻", "🌷", "🌺", "🍎", "🍊", "🍇", "🍓",
        "🚗", "🌿", "☎️", "⚽", "🦆", "🏠", "⭐", "🌙",
        "🎂", "☕", "🐰", "🦜"
    )

    init {
        // Observe saved preferences and update game state accordingly
        coroutineScope.launch { // Use injected coroutineScope
            combine(
                userPreferencesRepository.selectedDifficulty,
                userPreferencesRepository.bestScores
            ) { difficulty, bestScores ->
                val currentBestScore = bestScores[difficulty] ?: Int.MAX_VALUE
                val currentState = _gameState.value

                if (currentState.cards.isEmpty() || currentState.difficulty != difficulty) {
                    startNewGameInternal(difficulty, currentBestScore) // Changed to internal call
                } else {
                    _gameState.value = currentState.copy(bestScore = currentBestScore)
                }
            }.collect { }
        }
    }

    // Internal function to set up game state without triggering preference save
    private fun startNewGameInternal(difficulty: GameDifficulty, bestScore: Int) {
        val totalCards = difficulty.gridSize.first * difficulty.gridSize.second
        val pairs = totalCards / 2
        val selectedImages = cardImages.take(pairs)
        val cardPairs = selectedImages.flatMapIndexed { index, image ->
            listOf(
                Card(id = index * 2, imageRes = image),
                Card(id = index * 2 + 1, imageRes = image)
            )
        }.shuffled()

        _gameState.value = GameState(
            cards = cardPairs,
            difficulty = difficulty,
            bestScore = bestScore
        )
    }

    // Public function to start a new game, also saves preference
    fun startNewGame(difficulty: GameDifficulty) {
        coroutineScope.launch { // Use injected coroutineScope
            userPreferencesRepository.saveDifficulty(difficulty)
        }
        // Best score will be loaded by the init block's combine or preserved from current state
        startNewGameInternal(difficulty, _gameState.value.bestScore)
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
            handleMatch(currentState, flippedCards, newMoves)
        } else {
            handleMismatch(currentState, flippedCards, newMoves)
        }
    }

    private fun handleMatch(
        currentState: GameState,
        flippedCards: List<Card>,
        newMoves: Int
    ) {
        val updatedCards = currentState.cards.map { card ->
            if (flippedCards.any { it.id == card.id }) {
                card.copy(isMatched = true, isFlipped = true)
            } else {
                card
            }
        }

        val newMatchedPairs = currentState.matchedPairs + 1
        val totalPairs = currentState.difficulty.gridSize.first * currentState.difficulty.gridSize.second / 2
        val isGameComplete = newMatchedPairs == totalPairs

        val newBestScore = if (isGameComplete && newMoves < currentState.bestScore) {
            coroutineScope.launch { // Use injected coroutineScope
                userPreferencesRepository.saveBestScore(currentState.difficulty, newMoves)
            }
            newMoves
        } else {
            currentState.bestScore
        }

        _gameState.value = currentState.copy(
            cards = updatedCards,
            flippedCards = emptyList(),
            matchedPairs = newMatchedPairs,
            moves = newMoves,
            isGameComplete = isGameComplete,
            bestScore = newBestScore
        )
    }

    private fun handleMismatch(
        currentState: GameState,
        flippedCards: List<Card>,
        newMoves: Int
    ) {
        coroutineScope.launch { // Use injected coroutineScope
            delay(1000)
            val latestState = _gameState.value
            val updatedCards = latestState.cards.map { card ->
                if (flippedCards.any { it.id == card.id }) {
                    card.copy(isFlipped = false)
                } else {
                    card
                }
            }
            _gameState.value = latestState.copy(
                cards = updatedCards,
                flippedCards = emptyList(),
                moves = newMoves
            )
        }
    }

    fun resetGame() {
        // When resetting, use the current difficulty and its associated best score.
        // The init block's combine should ensure bestScore is up-to-date for the current difficulty.
        startNewGameInternal(_gameState.value.difficulty, _gameState.value.bestScore)
    }

    fun dismissGameCompleteDialog() {
        _gameState.value = _gameState.value.copy(isGameComplete = false)
    }

    fun cleanup() {
        coroutineScope.cancel() // Cancel the injected scope if GameManager is managing its lifecycle.
                               // Note: If scope is from viewModelScope or lifecycleScope, it's auto-managed.
    }
}
