package com.forzagundem.socialcanvas.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NewsSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        ForzaNewsRepository.refresh(applicationContext)
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }
}
