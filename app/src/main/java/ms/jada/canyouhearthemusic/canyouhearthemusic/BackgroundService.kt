package ms.jada.canyouhearthemusic.canyouhearthemusic

import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.util.Log
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.media.AudioDeviceInfo
import android.support.v4.app.NotificationManagerCompat
import ms.jada.canyouhearthemusic.canyouhearthemusic.AudioOutputReceiver.Companion.INTERNAL_OUTPUT
import java.util.*
import android.hardware.SensorManager



class BackgroundService : NotificationListenerService(), SensorEventListener {


    companion object {
        val STATE_CHANGE_EXTRA = "SCE"

        val OUTPUT_CHANGED = 4
        val SCREEN_CHANGED = 5

        val OUTPUT_NOTIFICATION_ID = 921

        val OUTPUT_SELECTED = 454
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    val aor = AudioOutputReceiver()
    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null

    override fun onCreate() {
        super.onCreate()

        Log.d("BG","Service Started")

        val audioIntentFilter = IntentFilter()
        audioIntentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        audioIntentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG)
        audioIntentFilter.addAction(AudioManager.ACTION_HDMI_AUDIO_PLUG)
        audioIntentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        audioIntentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        registerReceiver(aor, audioIntentFilter)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?;
        mProximity = mSensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mSensorManager!!.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)

    }

    var screenState = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return super.onStartCommand(intent, flags, startId)

        val changed = intent.hasExtra(STATE_CHANGE_EXTRA)
        if (!changed) {
            return super.onStartCommand(intent, flags, startId)
        }

        val change = intent.getIntExtra(STATE_CHANGE_EXTRA, -1)

        when(change) {
            OUTPUT_CHANGED -> {
                val output = intent.getIntExtra(AudioOutputReceiver.OUTPUT_TYPE, -1)

                Log.d("Output changed", output.toString())
                when (output) {
                    INTERNAL_OUTPUT, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
                    AudioDeviceInfo.TYPE_HDMI,
                    AudioDeviceInfo.TYPE_TELEPHONY, AudioDeviceInfo.TYPE_UNKNOWN -> {

                    }
                    else -> {
                        showOutputNotification()
                        askOutput()
                    }
                }
            }
            OUTPUT_SELECTED -> {
                Log.d("BS", "OMGOMGOMGOMGOMG")
            }
            SCREEN_CHANGED -> {
                Log.i("BS", "SCREEN_CHANGED")
                screenState = intent.getIntExtra(ScreenStateReceiver.SCREEN_CHANGE, -1)
            }
        }

        return Service.START_STICKY
    }

    private fun askOutput() {
        val intent = Intent(this, OutputDialog::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showOutputNotification() {
        val intent = Intent(this, OutputDialog::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        createNotificationChannel()

        val mBuilder = NotificationCompat.Builder(this, "Headphone Type")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("What device are you using to listen?")
                .setContentText("Thank you")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(this)

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(OUTPUT_NOTIFICATION_ID, mBuilder.build())
    }

    private fun action(type : String, intent : PendingIntent) : NotificationCompat.Action {
        val builder = NotificationCompat.Action.Builder(0,type, intent)
//        builder.
        return builder.build()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Headphone Type"
            val description = "Is music playing through speakers or headphones"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Headphone Type", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(aor)
        super.onDestroy()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val proximity = event!!.values[0]
        Log.d("BG","Sensor Changed $proximity");
    }
}
