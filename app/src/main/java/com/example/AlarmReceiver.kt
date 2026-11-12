package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import androidx.core.app.NotificationCompat

object AlarmTonePlayer {
    private var activeTrack: AudioTrack? = null
    private var mediaPlayer: android.media.MediaPlayer? = null

    fun playSound(context: android.content.Context, uriString: String, tonePresetFallback: String) {
        stopTone()
        val attributionContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                context.createAttributionContext("audio_playback")
            } catch (e: Exception) {
                context
            }
        } else {
            context
        }

        // 1. Try playing as content URI (from actual device file upload)
        if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
            try {
                mediaPlayer = android.media.MediaPlayer().apply {
                    setDataSource(attributionContext, android.net.Uri.parse(uriString))
                    prepare()
                    start()
                }
                // Stop after 60 seconds (1 minute max duration)
                Thread {
                    try {
                        Thread.sleep(60000)
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            stopTone()
                        }
                    } catch (e: Exception) {}
                }.start()
                return
            } catch (e: Exception) {
                e.printStackTrace()
                // Fail to play local URI (e.g. permission or format error), fallback to preset-based synth tone
            }
        }

        // 2. Fall back to synthesised programmatic audio tone
        val toneName = when (uriString) {
            "preset_luxury" -> "Luxury Entrance"
            "preset_badge" -> "Relaxing Tone"
            "preset_celebration" -> "Cheering Sound"
            "preset_notification" -> "Cute Tone"
            else -> tonePresetFallback
        }
        playToneProg(attributionContext, toneName)
    }

    fun playToneProg(toneName: String) {
        playToneProg(null, toneName)
    }

    fun playToneProg(context: android.content.Context?, toneName: String) {
        stopTone()
        val attributionContext = if (context != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                context.createAttributionContext("audio_playback")
            } catch (e: Exception) {
                context
            }
        } else {
            context
        }

        Thread {
            try {
                val sampleRate = 44100
                val durationSeconds = 4.0
                val numSamples = (sampleRate * durationSeconds).toInt()
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)
                val random = java.util.Random()

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    when (toneName) {
                        "Cute Tones", "Cute Tone" -> {
                            // Cute high-pitched playful music box chime synth notes
                            val freq = when {
                                t < 0.8 -> 783.99 // G5
                                t < 1.6 -> 880.00 // A5
                                t < 2.4 -> 987.77 // B5
                                t < 3.2 -> 1046.50 // C6
                                else -> 1318.51 // E6
                            }
                            val vibrato = 1.0 + 0.12 * Math.sin(2 * Math.PI * 7.5 * t)
                            val amplitude = 0.3 * (1.0 - (t / durationSeconds))
                            sample[i] = Math.sin(2 * Math.PI * freq * t * vibrato) * amplitude
                        }
                        "Relaxing Tones", "Relaxing Tone" -> {
                            // Gentle warm organ sweep/flute chord
                            val baseFreq = 261.63 // C4
                            val chord1 = 329.63 // E4
                            val chord2 = 392.00 // G4
                            val waveVal = Math.sin(2 * Math.PI * baseFreq * t) +
                                          0.4 * Math.sin(2 * Math.PI * chord1 * t) +
                                          0.3 * Math.sin(2 * Math.PI * chord2 * t)
                            sample[i] = waveVal * 0.22 * (0.5 + 0.5 * Math.sin(Math.PI * t / durationSeconds))
                        }
                        "Dramatic Loud Sounds" -> {
                            // Loud rapid frequency sweep buzzer
                            val sweepFreq = 580.0 + 380.0 * Math.sin(2 * Math.PI * 5.0 * t)
                            val squareWave = if (Math.sin(2 * Math.PI * sweepFreq * t) > 0) 1.0 else -1.0
                            val staticNoise = random.nextDouble() * 0.25
                            sample[i] = (squareWave + staticNoise) * 0.4
                        }
                        "Clapping Sound", "Clapping" -> {
                            // Programmatic clapping noise bursts spaced 120ms apart with exponential decay
                            val interval = (sampleRate * 0.120).toInt()
                            val burstOffset = i % interval
                            val damping = Math.exp(-burstOffset.toDouble() / (sampleRate * 0.04)) // fast decay
                            val noise = (random.nextFloat() * 2.0f - 1.0f)
                            sample[i] = noise * 0.35 * damping * (1.0 - (t / durationSeconds))
                        }
                        "Cheering Sound", "Cheering" -> {
                            // Programmatic sweeping waves representing voices cheering WHOOO!
                            val sweep1 = 350.0 + 450.0 * (t / durationSeconds) + 40.0 * Math.sin(2 * Math.PI * 18.0 * t)
                            val sweep2 = 420.0 + 520.0 * (t / durationSeconds) + 30.0 * Math.sin(2 * Math.PI * 22.0 * t)
                            val noiseBg = (random.nextFloat() * 2.0f - 1.0f) * 0.12
                            val amplitude = 0.25 * Math.sin(Math.PI * t / durationSeconds)
                            sample[i] = (Math.sin(2 * Math.PI * sweep1 * t) + Math.sin(2 * Math.PI * sweep2 * t) + noiseBg) * amplitude
                        }
                        "Luxury Entrance" -> {
                            // Majestic luxury bell arpeggios & warm royal swelling chord sequence (Cmaj7 -> Dmin7 -> Fmaj7)
                            val chord = when {
                                t < 1.0 -> listOf(261.63, 329.63, 392.00, 493.88)
                                t < 2.0 -> listOf(293.66, 349.23, 440.00, 587.33)
                                t < 3.0 -> listOf(349.23, 440.00, 523.25, 659.25)
                                else -> listOf(392.00, 493.88, 587.33, 783.99)
                            }
                            var v = 0.0
                            chord.forEachIndexed { idx, freq ->
                                val noteStart = idx * 0.25
                                if (t >= noteStart) {
                                    val elapsed = t - noteStart
                                    val decay = Math.exp(-elapsed * 1.5)
                                    v += Math.sin(2 * Math.PI * freq * elapsed) * decay * 0.15
                                }
                            }
                            sample[i] = v * (1.0 - (t / durationSeconds))
                        }
                        else -> {
                            sample[i] = Math.sin(2 * Math.PI * 440.0 * t) * 0.25
                        }
                    }
                }

                var idx = 0
                for (dVal in sample) {
                    val valInt = (Math.max(-1.0, Math.min(1.0, dVal)) * 32767).toInt().toShort()
                    generatedSnd[idx++] = (valInt.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = ((valInt.toInt() and 0xff00) ushr 8).toByte()
                }

                val track = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val attributes = android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    val format = android.media.AudioFormat.Builder()
                        .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                    val builder = android.media.AudioTrack.Builder()
                        .setAudioAttributes(attributes)
                        .setAudioFormat(format)
                        .setBufferSizeInBytes(generatedSnd.size)
                        .setTransferMode(android.media.AudioTrack.MODE_STATIC)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && attributionContext != null) {
                        try {
                            builder.setContext(attributionContext)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                    builder.build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        android.media.AudioManager.STREAM_MUSIC,
                        sampleRate,
                        android.media.AudioFormat.CHANNEL_OUT_MONO,
                        android.media.AudioFormat.ENCODING_PCM_16BIT,
                        generatedSnd.size,
                        AudioTrack.MODE_STATIC
                    )
                }

                activeTrack = track
                track.write(generatedSnd, 0, generatedSnd.size)
                track.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun stopTone() {
        try {
            activeTrack?.stop()
            activeTrack?.release()
            activeTrack = null
        } catch (e: Exception) {}
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {}
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isTask = intent.getBooleanExtra("is_task_reminder", false)
        if (isTask) {
            val taskName = intent.getStringExtra("task_name") ?: "Pretty Scheduled Task"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "task_planner_reminders"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Task Planner Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Chimes task reminders exactly on selected times"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("📋 Task Reminder: $taskName")
                .setContentText("Your scheduled time has arrived! Open App to view details and checkoff task. 💕✨")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            AlarmTonePlayer.playSound(context, "preset_notification", "Cute Tone")

            try {
                notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        val label = intent.getStringExtra("alarm_label") ?: "Cute Alarm 🕰️"
        val tone = intent.getStringExtra("alarm_tone") ?: "Cute Tones"

        // Play the synthesized audio track!
        AlarmTonePlayer.playToneProg(context, tone)

        // Trigger native notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "cute_clock_alarms"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cozy Clock Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chimes beautiful custom alarms"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕰️ Cute Alarm: $label")
            .setContentText("Tap to dim chimes or manage your pretty planner! 💕✨")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(88912, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
