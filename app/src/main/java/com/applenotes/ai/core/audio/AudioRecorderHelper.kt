package com.applenotes.ai.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime: Long = 0L

    fun startRecording(): File {
        val dir = File(context.filesDir, "audio_notes")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "audio_${System.currentTimeMillis()}.m4a")
        currentRecordingFile = file
        recordingStartTime = System.currentTimeMillis()

        recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        return file
    }

    fun getRecordingElapsedSeconds(): Long {
        return if (recordingStartTime > 0) (System.currentTimeMillis() - recordingStartTime) / 1000 else 0L
    }

    fun getMaxAmplitude(): Int {
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun stopRecording(): String? {
        recordingStartTime = 0L
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            currentRecordingFile?.absolutePath
        } catch (e: Exception) {
            recorder = null
            null
        }
    }

    fun playAudio(filePath: String, onCompletion: () -> Unit) {
        stopPlaying()
        try {
            player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    onCompletion()
                }
                start()
            }
        } catch (e: Exception) {
            onCompletion()
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            player?.seekTo(positionMs)
        } catch (e: Exception) {
            // Ignore seek errors if player is not ready
        }
    }

    fun getCurrentPosition(): Int = player?.currentPosition ?: 0

    fun stopPlaying() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }

    fun isPlaying(): Boolean = player?.isPlaying == true

    fun getAudioDuration(filePath: String): Long {
        return try {
            val mmr = android.media.MediaMetadataRetriever()
            mmr.setDataSource(filePath)
            val durationStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            mmr.release()
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}