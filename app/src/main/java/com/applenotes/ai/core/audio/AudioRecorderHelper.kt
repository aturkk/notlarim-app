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

    fun startRecording(): File {
        val dir = File(context.filesDir, "audio_notes")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "audio_.m4a")
        currentRecordingFile = file

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

    fun stopRecording(): String? {
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

    fun stopPlaying() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }

    fun isPlaying(): Boolean = player?.isPlaying == true
}