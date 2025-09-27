package com.liteobject.mindpairs.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liteobject.mindpairs.model.Card as GameCard

@Composable
fun MemoryCard(
    card: GameCard,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(600),
        label = "card_flip"
    )

    val alpha by animateFloatAsState(
        targetValue = if (card.isMatched) 0.7f else 1f,
        animationSpec = tween(300),
        label = "card_alpha"
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                this.alpha = alpha
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched) {
                onCardClick(card.id)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (card.isFlipped || card.isMatched) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (card.isMatched) Color.Cyan else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (card.isFlipped || card.isMatched) {
                Text(
                    text = card.imageRes,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "?",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
