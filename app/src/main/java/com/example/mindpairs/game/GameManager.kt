package com.example.mindpairs.game

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
import kotlinx.coroutines.launch

class GameManager {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val gameScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val cardImages = listOf(
        "🌹", "🌻", "🌷", "🌺", "🍎", "🍊", "🍇", "🍓",
        "🚗", "🌿", "☎️", "⚽", "🦆", "🏠", "⭐", "🌙",
        "🎂", "☕", "🐰", "🦜"
    )

    fun startNewGame(difficulty: GameDifficulty) {
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
            bestScore = _gameState.value.bestScore // Preserve best score
        )
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
        gameScope.launch {
            delay(1000)
            val latestState = _gameState.value // Get latest state after delay
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
        startNewGame(_gameState.value.difficulty)
    }

    fun dismissGameCompleteDialog() {
        _gameState.value = _gameState.value.copy(isGameComplete = false)
    }

    fun cleanup() {
        gameScope.cancel()
    }
}
