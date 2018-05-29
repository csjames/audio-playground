package ms.jada.canyouhearthemusic.canyouhearthemusic

import android.Manifest
import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Visualizer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {


    val aor = AudioOutputReceiver()
    lateinit var equalizer : Equalizer

    private val TAG: String = "MA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioRecord()
//        requestNotificationAccess()
        requestAccessSettingsAccess()

        val i = Intent(this, BackgroundService::class.java)
        startService(i)

        equalizer = Equalizer(Int.MAX_VALUE,0)
        equalizer.enabled = true

        var value = 0
        down.setOnClickListener {
            value -= 200

            Log.d(TAG, value.toString())
            Log.d(TAG, Arrays.toString(equalizer.bandLevelRange))

            Log.d(TAG, equalizer.numberOfBands.toString())

            for (band in 0 until equalizer.numberOfBands) {
                equalizer.setBandLevel(band.toShort(), value.toShort())
            }

        }

        up.setOnClickListener {

            value += 200

            Log.d(TAG, value.toString())
            Log.d(TAG, Arrays.toString(equalizer.bandLevelRange))

            Log.d(TAG, equalizer.numberOfBands.toString())

            for (band in 0 until equalizer.numberOfBands) {
                equalizer.setBandLevel(band.toShort(), value.toShort())
            }

        }

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        down2.setOnClickListener {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, FLAG_REMOVE_SOUND_AND_VIBRATE
            )
        }

        up2.setOnClickListener {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, FLAG_REMOVE_SOUND_AND_VIBRATE
            )
        }

        vis.setOnClickListener {
            captureFFT()
        }

    }

    fun captureFFT(){

        fftView.init()

        try {

//            val timer = Timer()

            val vis = Visualizer(0) // all output streams. This may include phone calls and such.

            vis!!.enabled = false

            vis!!.measurementMode = Visualizer.MEASUREMENT_MODE_PEAK_RMS
            vis!!.scalingMode = Visualizer.SCALING_MODE_AS_PLAYED

            Log.d("MinMax", Visualizer.getCaptureSizeRange()[0].toString())
            Log.d("MinMax", Visualizer.getCaptureSizeRange()[1].toString())

            vis!!.captureSize = Visualizer.getCaptureSizeRange()[1]

            vis.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
//                    Log.d(TAG,"Got fft")
//
//                    Log.d(TAG, fft!![0].toString())

                    fftView.setFFTData(fft)
                    Log.d(TAG, fft!![0].toString() + " " + fft!![2].toString() )
                }

                override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
//                    Log.d(TAG,"Got fft")
//
//                    Log.d(TAG, waveform!![0].toString())
//
//                    fftView.setFFTData(waveform)

                }

            }, 4*1000, true, true)


            vis!!.enabled = true

        } catch (e : Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    override fun onDestroy() {
        equalizer.release()
        super.onDestroy()
    }

    val requestAudioRecord =  fun () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perm = checkForPermission(arrayOf<String>(Manifest.permission.INTERNET,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.RECORD_AUDIO), 0, R.string.app_name)

        }
    }

    private val requestNotificationAccess = fun () {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), 5)
            } else {
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                startActivityForResult(intent, 5)
            }
    }

    private val requestAccessSettingsAccess = fun() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && !hasUsageStatsPermission(this)) {
            startActivityForResult(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 5)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun checkForPermission(permissions: Array<String>, permRequestCode: Int, msgResourceId: Int): Boolean {
        val permissionsNeeded = arrayListOf<String>()
        for (i in 0 .. permissions.size -1 ) {
            val perm = permissions[i]
            if (ContextCompat.checkSelfPermission(this@MainActivity, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(perm)
            }
        }

        return if (permissionsNeeded.size > 0) {
            requestPermissions(permissionsNeeded.toTypedArray(), permRequestCode)
            false
        } else {
            true
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun hasUsageStatsPermission(context : Context) : Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

}
