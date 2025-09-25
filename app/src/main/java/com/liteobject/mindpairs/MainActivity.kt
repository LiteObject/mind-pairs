package com.liteobject.mindpairs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.liteobject.mindpairs.data.UserPreferencesRepository
import com.liteobject.mindpairs.game.GameManager
import com.liteobject.mindpairs.ui.screens.AboutScreen
import com.liteobject.mindpairs.ui.screens.GameScreen
import com.liteobject.mindpairs.ui.theme.MindPairsTheme

object Routes {
    const val GAME = "game"
    const val ABOUT = "about"
}

@OptIn(ExperimentalMaterial3Api::class)
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

                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MindPairsTopAppBar(navController = navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.GAME,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.GAME) {
                            GameScreen(
                                gameManager = gameManager
                            )
                        }
                        composable(Routes.ABOUT) {
                            AboutScreen()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindPairsTopAppBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TopAppBar(
        title = { Text("Mind Pairs") },
        navigationIcon = {
            if (currentRoute == Routes.ABOUT) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (currentRoute == Routes.GAME) {
                IconButton(onClick = { navController.navigate(Routes.ABOUT) }) {
                    Icon(Icons.Filled.Info, contentDescription = "About")
                }
            }
        }
    )
}
