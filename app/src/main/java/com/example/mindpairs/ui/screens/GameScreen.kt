package com.example.mindpairs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindpairs.game.GameManager
import com.example.mindpairs.model.GameDifficulty
import com.example.mindpairs.ui.components.MemoryCard

@Composable
fun GameScreen(
    gameManager: GameManager,
    modifier: Modifier = Modifier
) {
    val gameState by gameManager.gameState.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    LaunchedEffect(Unit) {
        gameManager.startNewGame(GameDifficulty.MEDIUM)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game Header
        Text(
            text = "Memory Match-Up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Difficulty Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GameDifficulty.entries.forEach { difficulty ->
                FilterChip(
                    onClick = { gameManager.startNewGame(difficulty) },
                    label = {
                        Text(
                            text = difficulty.displayName,
                            fontSize = 12.sp
                        )
                    },
                    selected = gameState.difficulty == difficulty,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Game Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                title = "Moves",
                value = gameState.moves.toString(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )

            StatCard(
                title = "Pairs Found",
                value = gameState.matchedPairs.toString(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )

            if (gameState.bestScore != Int.MAX_VALUE) {
                StatCard(
                    title = "Best Score",
                    value = gameState.bestScore.toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }

        // Calculate available space for grid
        val headerAndStatsHeight = 220.dp
        val availableGridHeight = screenHeight - headerAndStatsHeight

        val gridRows = when (gameState.difficulty) {
            GameDifficulty.EASY -> 3
            GameDifficulty.MEDIUM -> 4
            GameDifficulty.HARD -> 5
        }

        val spacing = 8.dp
        val totalSpacing = spacing * (gridRows + 1)
        val maxCardHeight = (availableGridHeight - totalSpacing) / gridRows

        // Game Grid with responsive sizing
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val gridHeight = minOf(availableGridHeight, maxHeight)

            LazyVerticalGrid(
                columns = GridCells.Fixed(gameState.difficulty.gridSize.first),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight)
            ) {
                items(gameState.cards) { card ->
                    MemoryCard(
                        card = card,
                        onCardClick = { cardId ->
                            gameManager.flipCard(cardId)
                        },
                        modifier = Modifier
                            .height(maxCardHeight.coerceAtMost(120.dp))
                    )
                }
            }
        }

        // Control Buttons
        Button(
            onClick = { gameManager.resetGame() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(48.dp)
        ) {
            Text(
                text = "New Game",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Game Complete Dialog
        if (gameState.isGameComplete) {
            GameCompleteDialog(
                moves = gameState.moves,
                isNewBestScore = gameState.moves == gameState.bestScore,
                onPlayAgain = { gameManager.resetGame() }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .weight(1f)
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GameCompleteDialog(
    moves: Int,
    isNewBestScore: Boolean,
    onPlayAgain: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "Congratulations! 🎉",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "You completed the game in $moves moves!",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (isNewBestScore) {
                    Text(
                        text = "🏆 New Best Score! 🏆",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onPlayAgain) {
                Text("Play Again")
            }
        }
    )
}
