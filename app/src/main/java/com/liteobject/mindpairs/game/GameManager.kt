package com.liteobject.mindpairs.game

import com.liteobject.mindpairs.data.UserPreferencesRepository
import com.liteobject.mindpairs.model.Card
import com.liteobject.mindpairs.model.GameDifficulty
import com.liteobject.mindpairs.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var flipJob: Job? = null

    // Emoji pairs for the memory game
    private val emojiPairs = listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
        "🐨", "🐯", "🦁", "🐸", "🐵", "🐔", "🐧", "🐦",
        "🌸", "🌺", "🌻", "🌷", "🌹", "🌼", "🌻", "🌲",
        "🍎", "🍊", "🍌", "🍇", "🍓", "🍑", "🍒", "🥝"
    )

    init {
        startNewGame(GameDifficulty.MEDIUM)
    }

    fun startNewGame(difficulty: GameDifficulty) {
        coroutineScope.launch {
            val bestScore = userPreferencesRepository.getBestScore(difficulty.name.lowercase()).first()

            val totalCards = difficulty.gridSize.first * difficulty.gridSize.second
            val pairs = totalCards / 2

            // Select random emojis for this game
            val selectedEmojis = emojiPairs.shuffled().take(pairs)
            val gameCards = selectedEmojis.flatMap { emoji ->
                listOf(
                    Card(id = selectedEmojis.indexOf(emoji) * 2, imageRes = emoji),
                    Card(id = selectedEmojis.indexOf(emoji) * 2 + 1, imageRes = emoji)
                )
            }.shuffled().mapIndexed { index, card ->
                card.copy(id = index)
            }

            _gameState.value = GameState(
                cards = gameCards,
                difficulty = difficulty,
                bestScore = bestScore
            )
        }
    }

    fun resetGame() {
        startNewGame(_gameState.value.difficulty)
    }

    fun flipCard(cardId: Int) {
        val currentState = _gameState.value
        if (currentState.flippedCards.size >= 2 || currentState.isGameComplete) return

        val card = currentState.cards.find { it.id == cardId }
        if (card == null || card.isFlipped || card.isMatched) return

        val updatedCards = currentState.cards.map {
            if (it.id == cardId) it.copy(isFlipped = true) else it
        }
        val flippedCards = updatedCards.filter { it.isFlipped && !it.isMatched }

        _gameState.value = currentState.copy(
            cards = updatedCards,
            flippedCards = flippedCards,
            moves = if (flippedCards.size == 1) currentState.moves + 1 else currentState.moves
        )

        if (flippedCards.size == 2) {
            checkForMatch()
        }
    }

    private fun checkForMatch() {
        flipJob?.cancel()
        flipJob = coroutineScope.launch {
            delay(1000) // Show cards for 1 second

            val currentState = _gameState.value
            val flippedCards = currentState.flippedCards

            if (flippedCards.size == 2) {
                val isMatch = flippedCards[0].imageRes == flippedCards[1].imageRes

                val updatedCards = currentState.cards.map { card ->
                    when {
                        isMatch && flippedCards.any { it.id == card.id } ->
                            card.copy(isMatched = true, isFlipped = true)
                        !isMatch && flippedCards.any { it.id == card.id } ->
                            card.copy(isFlipped = false)
                        else -> card
                    }
                }

                val newMatchedPairs = if (isMatch) currentState.matchedPairs + 1 else currentState.matchedPairs
                val totalPairs = currentState.difficulty.gridSize.first * currentState.difficulty.gridSize.second / 2
                val isGameComplete = newMatchedPairs >= totalPairs

                var newBestScore = currentState.bestScore
                if (isGameComplete && currentState.moves < currentState.bestScore) {
                    newBestScore = currentState.moves
                    coroutineScope.launch {
                        userPreferencesRepository.saveBestScore(
                            currentState.difficulty.name.lowercase(),
                            currentState.moves
                        )
                    }
                }

                _gameState.value = currentState.copy(
                    cards = updatedCards,
                    flippedCards = emptyList(),
                    matchedPairs = newMatchedPairs,
                    isGameComplete = isGameComplete,
                    bestScore = newBestScore
                )
            }
        }
    }

    fun dismissGameCompleteDialog() {
        _gameState.value = _gameState.value.copy(isGameComplete = false)
    }
}
