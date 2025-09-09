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
import androidx.compose.ui.platform.LocalContext
import com.example.mindpairs.data.UserPreferencesRepository
import com.example.mindpairs.game.GameManager
import com.example.mindpairs.ui.screens.GameScreen
import com.example.mindpairs.ui.theme.MindPairsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindPairsTheme {
                val context = LocalContext.current
                val userPreferencesRepository = remember { UserPreferencesRepository(context) }
                val gameManager = remember { GameManager(userPreferencesRepository) }

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
