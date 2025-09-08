package com.example.mindpairs.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.* // Includes statusBarsPadding, IntrinsicSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindpairs.game.GameManager
import com.example.mindpairs.model.GameDifficulty
import com.example.mindpairs.ui.components.MemoryCard
import kotlin.math.min

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GameScreen(
    gameManager: GameManager,
    modifier: Modifier = Modifier
) {
    val gameState by gameManager.gameState.collectAsState()

    // LaunchedEffect that forced MEDIUM difficulty has been removed.
    // GameManager now handles loading initial difficulty from DataStore.

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding() // Ensures content is below status bar
            .padding(16.dp), // Overall padding for the screen content
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp) // Consistent spacing for direct children
    ) {
        // Game Header
        Text(
            text = "Memory Match-Up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
            // Removed individual padding, handled by Column's verticalArrangement
        )

        // Difficulty Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()), // Makes difficulty chips scrollable
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GameDifficulty.entries.forEach { difficulty ->
                FilterChip(
                    onClick = { gameManager.startNewGame(difficulty) },
                    label = { Text(text = difficulty.displayName, fontSize = 12.sp) },
                    selected = gameState.difficulty == difficulty,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Game Stats Row - Modified for consistent height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min), // All StatCards will have the same height
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(label = "Moves", value = "${gameState.moves}", modifier = Modifier.weight(1f).fillMaxHeight())
            StatCard(label = "Pairs Found", value = "${gameState.matchedPairs}", modifier = Modifier.weight(1f).fillMaxHeight())
            if (gameState.bestScore != Int.MAX_VALUE) {
                StatCard(label = "Best Score", value = "${gameState.bestScore}", modifier = Modifier.weight(1f).fillMaxHeight(), isTertiary = true)
            }
        }

        // Game Grid with responsive sizing
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f) // Takes up remaining vertical space
                .fillMaxWidth()
        ) {
            val gridRows = when (gameState.difficulty) {
                GameDifficulty.EASY -> 3
                GameDifficulty.MEDIUM -> 4
                GameDifficulty.HARD -> 5 // Corresponds to 4x5 grid defined in GameDifficulty
            }
            val columnCount = gameState.difficulty.gridSize.first
            val spacing = 8.dp
            val cardAspectRatio = 0.75f

            // Calculate height constraint from available vertical space
            val totalVerticalSpacing = spacing * (gridRows + 1)
            val heightFromVerticalConstraint = (this.maxHeight - totalVerticalSpacing) / gridRows

            // Calculate height constraint from available horizontal space (per column)
            val totalHorizontalSpacing = spacing * (columnCount + 1)
            val cellWidth = (this.maxWidth - totalHorizontalSpacing) / columnCount
            val heightFromHorizontalConstraint = cellWidth / cardAspectRatio

            // Final card height is the minimum of the two constraints to fit the cell perfectly
            val finalCardHeight = min(heightFromVerticalConstraint.value, heightFromHorizontalConstraint.value).dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                contentPadding = PaddingValues(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(this.maxHeight) // Use full height provided by BoxWithConstraints
            ) {
                items(gameState.cards) { card ->
                    MemoryCard(
                        card = card,
                        onCardClick = { cardId -> gameManager.flipCard(cardId) },
                        modifier = Modifier
                            .height(finalCardHeight.coerceAtMost(120.dp)) // Coerce as a safeguard
                            .aspectRatio(cardAspectRatio)
                    )
                }
            }
        }

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { gameManager.resetGame() },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(48.dp)
            ) {
                Text(text = "New Game", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
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
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, isTertiary: Boolean = false) {
    Card(
        modifier = modifier.padding(4.dp), // Modifier from call site includes .weight(1f) and .fillMaxHeight()
        colors = CardDefaults.cardColors(
            containerColor = if (isTertiary) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the Card, which has been given a specific size by the Row
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically within the card
        ) {
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTertiary) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GameCompleteDialog(moves: Int, isNewBestScore: Boolean, onPlayAgain: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal by clicking outside */ },
        title = {
            Text(
                text = "Congratulations! 🎉",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
                Text("Play Again")
            }
        }
    )
}
