package com.example.mindpairs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.mindpairs.game.GameManager
import com.example.mindpairs.ui.screens.GameScreen
import com.example.mindpairs.ui.theme.MindPairsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindPairsTheme {
                val gameManager = remember { GameManager() }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        gameManager = gameManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
