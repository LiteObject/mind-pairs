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

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Memory Match-Up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(label = "Moves", value = "${gameState.moves}", modifier = Modifier.weight(1f).fillMaxHeight())
            StatCard(label = "Pairs Found", value = "${gameState.matchedPairs}", modifier = Modifier.weight(1f).fillMaxHeight())
            if (gameState.bestScore != Int.MAX_VALUE) {
                StatCard(label = "Best Score", value = "${gameState.bestScore}", modifier = Modifier.weight(1f).fillMaxHeight(), isTertiary = true)
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val gridRows = when (gameState.difficulty) {
                GameDifficulty.EASY -> 3
                GameDifficulty.MEDIUM -> 4
                GameDifficulty.HARD -> 5
            }
            val columnCount = gameState.difficulty.gridSize.first
            val spacing = 8.dp
            val cardAspectRatio = 0.75f

            val totalVerticalSpacing = spacing * (gridRows + 1)
            val heightFromVerticalConstraint = (this.maxHeight - totalVerticalSpacing) / gridRows

            val totalHorizontalSpacing = spacing * (columnCount + 1)
            val cellWidth = (this.maxWidth - totalHorizontalSpacing) / columnCount
            val heightFromHorizontalConstraint = cellWidth / cardAspectRatio

            val finalCardHeight = min(heightFromVerticalConstraint.value, heightFromHorizontalConstraint.value).dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                contentPadding = PaddingValues(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(this.maxHeight)
            ) {
                items(gameState.cards) { card ->
                    MemoryCard(
                        card = card,
                        onCardClick = { cardId -> gameManager.flipCard(cardId) },
                        modifier = Modifier
                            .height(finalCardHeight.coerceAtMost(120.dp))
                            .aspectRatio(cardAspectRatio)
                    )
                }
            }
        }

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

        if (gameState.isGameComplete) {
            GameCompleteDialog(
                moves = gameState.moves,
                isNewBestScore = gameState.moves == gameState.bestScore,
                onPlayAgain = { gameManager.resetGame() },
                onDismiss = { gameManager.dismissGameCompleteDialog() }
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, isTertiary: Boolean = false) {
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTertiary) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
private fun GameCompleteDialog(
    moves: Int, 
    isNewBestScore: Boolean, 
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Congratulations!",
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
                        text = "New Best Score!",
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
        },
        dismissButton = { 
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text("Dismiss")
            }
        }
    )
}
