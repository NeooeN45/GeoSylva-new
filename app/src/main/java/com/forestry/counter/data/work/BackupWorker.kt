package com.forestry.counter.data.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.forestry.counter.ForestryCounterApplication
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sauvegarde atomique de l'export portable des compteurs.
 *
 * La base Room est fournie par l'Application : le worker ne doit jamais rouvrir
 * le fichier SQLCipher sans sa SupportFactory.
 */
class BackupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? ForestryCounterApplication
            ?: return Result.failure(workDataOf(KEY_ERROR to "Application container unavailable"))

        val directory = applicationContext.getExternalFilesDir(BACKUP_DIRECTORY)
            ?: File(applicationContext.filesDir, BACKUP_DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) {
            return retryOrFail("Backup directory cannot be created")
        }

        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(Date())
        val target = File(directory, "GeoSylvaCounters-$timestamp.zip")
        val temporary = File(directory, ".${target.name}.tmp")

        return try {
            if (!resetTemporaryBackup(temporary)) {
                return retryOrFail("Stale temporary backup cannot be removed")
            }
            val exportResult = app.exportDataUseCase.exportToZipFile(temporary)
            if (exportResult.isFailure || !temporary.isFile || temporary.length() == 0L) {
                temporary.delete()
                retryOrFail(exportResult.exceptionOrNull()?.message ?: "Empty backup")
            } else if (!publishBackup(temporary, target)) {
                temporary.delete()
                retryOrFail("Atomic backup publication failed")
            } else {
                Result.success(
                    workDataOf(
                        KEY_BACKUP_PATH to target.absolutePath,
                        KEY_BACKUP_SCOPE to BACKUP_SCOPE_COUNTERS
                    )
                )
            }
        } catch (error: Exception) {
            temporary.delete()
            Log.e(TAG, "Automatic backup failed", error)
            retryOrFail(error.message ?: "Unexpected backup error")
        }
    }

    private fun retryOrFail(message: String): Result {
        val data = workDataOf(KEY_ERROR to message)
        return if (runAttemptCount + 1 >= MAX_ATTEMPTS) Result.failure(data) else Result.retry()
    }

    companion object {
        private const val TAG = "BackupWorker"
        private const val BACKUP_DIRECTORY = "backups"
        private const val MAX_ATTEMPTS = 3

        const val KEY_BACKUP_PATH = "backup_path"
        const val KEY_BACKUP_SCOPE = "backup_scope"
        const val KEY_ERROR = "error"
        const val BACKUP_SCOPE_COUNTERS = "counters"
    }
}

/** Supprime un reste temporaire avant export, ou échoue sans l'écraser. */
internal fun resetTemporaryBackup(temporary: File): Boolean =
    !temporary.exists() || temporary.delete()

/** Publie une sauvegarde complète par déplacement atomique, sans écraser une archive existante. */
internal fun publishBackup(temporary: File, target: File): Boolean {
    if (!temporary.isFile || temporary.length() == 0L || target.exists()) {
        return false
    }
    return try {
        Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        true
    } catch (_: IOException) {
        false
    }
}
