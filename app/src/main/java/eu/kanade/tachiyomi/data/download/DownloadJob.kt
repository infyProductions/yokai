package eu.kanade.tachiyomi.data.download

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.util.system.isConnectedToWifi
import eu.kanade.tachiyomi.util.system.isOnline
import eu.kanade.tachiyomi.util.system.tryToSetForeground
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy

/**
 * This worker is used to manage the downloader. The system can decide to stop the worker, in
 * which case the downloader is also stopped. It's also stopped while there's no network available.
 */
class DownloadJob(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val downloadManager: DownloadManager = Injekt.get()
    private val preferences: PreferencesHelper = Injekt.get()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val firstDL = downloadManager.queue.firstOrNull()
        val notification = DownloadNotifier(context).setPlaceholder(firstDL).build()
        val id = Notifications.ID_DOWNLOAD_CHAPTER
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(id, notification)
        }
    }

    override suspend fun doWork(): Result {
        tryToSetForeground()

        var networkCheck = checkConnectivity()
        var active = networkCheck
        if (active) {
            downloadManager.startDownloads()
        }

        // Keep the worker running when needed
        while (active) {
            delay(100)
            networkCheck = checkConnectivity()
            active = !isStopped && networkCheck && downloadManager.isRunning
        }
        return Result.success()
    }

    private fun checkConnectivity(): Boolean {
        return with(applicationContext) {
            if (isOnline()) {
                val noWifi = preferences.downloadOnlyOverWifi() && !isConnectedToWifi()
                if (noWifi) {
                    downloadManager.stopDownloads(applicationContext.getString(R.string.no_wifi_connection))
                }
                !noWifi
            } else {
                downloadManager.stopDownloads(applicationContext.getString(R.string.no_network_connection))
                false
            }
        }
    }

    companion object {
        private const val TAG = "Downloader"

        private val downloadChannel = Channel<Boolean>()
        val downloadFlow = downloadChannel.receiveAsFlow()

        fun start(context: Context) {
            val request = OneTimeWorkRequestBuilder<DownloadJob>()
                .addTag(TAG)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, request)
        }

        fun stop(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(TAG)
        }

        fun callListeners(downloading: Boolean? = null) {
            val downloadManager: DownloadManager by injectLazy()
            downloadChannel.trySend(downloading ?: !downloadManager.isPaused())
        }

        fun isRunning(context: Context): Boolean {
            return WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(TAG)
                .get()
                .let { list -> list.count { it.state == WorkInfo.State.RUNNING } == 1 }
        }
    }
}