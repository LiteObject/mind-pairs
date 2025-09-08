package com.example.mindpairs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // Added import
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Added import
import com.example.mindpairs.data.UserPreferencesRepository // Added import
import com.example.mindpairs.game.GameManager
import com.example.mindpairs.ui.screens.GameScreen
import com.example.mindpairs.ui.theme.MindPairsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindPairsTheme {
                val context = LocalContext.current.applicationContext
                val coroutineScope = rememberCoroutineScope()
                val userPreferencesRepository = remember { UserPreferencesRepository(context) }
                val gameManager = remember { GameManager(userPreferencesRepository, coroutineScope) }

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
