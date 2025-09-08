package com.example.mindpairs.game

import com.example.mindpairs.model.Card
import com.example.mindpairs.model.GameDifficulty
import com.example.mindpairs.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameManager {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Nostalgic themed images for older adults
    private val cardImages = listOf(
        "🌹", "🌻", "🌷", "🌺", "🍎", "🍊", "🍇", "🍓",
        "🚗", "📻", "☎️", "📷", "🎵", "🏠", "⭐", "🌙",
        "🎂", "☕", "🍯", "🕊️"
    )

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

        _gameState.value = GameState(
            cards = cardPairs,
            difficulty = difficulty,
            bestScore = _gameState.value.bestScore
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
            // Cards match - mark them as matched
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
        } else {
            // Cards don't match - flip them back after a delay
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000) // Show cards for 1 second

                val updatedCards = currentState.cards.map { card ->
                    if (flippedCards.any { it.id == card.id }) {
                        card.copy(isFlipped = false)
                    } else {
                        card
                    }
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
