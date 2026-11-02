package com.forzagundem.socialcanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.forzagundem.socialcanvas.data.TemplateType
import com.forzagundem.socialcanvas.ui.screens.EditorScreen
import com.forzagundem.socialcanvas.ui.screens.HomeScreen
import com.forzagundem.socialcanvas.ui.theme.Bg
import com.forzagundem.socialcanvas.ui.theme.SocialCanvasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialCanvasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Bg
                ) {
                    SocialCanvasApp()
                }
            }
        }
    }
}

@Composable
fun SocialCanvasApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onTemplateSelected = { template ->
                    navController.navigate("editor/${template.name}")
                }
            )
        }
        composable(
            route = "editor/{template}",
            arguments = listOf(navArgument("template") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("template") ?: TemplateType.BOS.name
            val template = runCatching { TemplateType.valueOf(name) }.getOrDefault(TemplateType.BOS)
            EditorScreen(
                initialTemplate = template,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
