package com.forzagundem.socialcanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.forzagundem.socialcanvas.data.NewsSyncWorker
import com.forzagundem.socialcanvas.ui.screens.HomeScreen
import com.forzagundem.socialcanvas.ui.theme.Bg
import com.forzagundem.socialcanvas.ui.theme.SocialCanvasTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleNewsSync()
        setContent {
            SocialCanvasTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
                    ForzaXApp()
                }
            }
        }
    }

    private fun scheduleNewsSync() {
        val request = PeriodicWorkRequestBuilder<NewsSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "forzax-rss-sync", ExistingPeriodicWorkPolicy.KEEP, request
        )
    }
}

@Composable
fun ForzaXApp() {
    HomeScreen()
}
