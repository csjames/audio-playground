package ms.jada.canyouhearthemusic.canyouhearthemusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioDeviceInfo.*
import android.media.AudioManager
import android.os.Build
import android.util.Log

class AudioOutputReceiver : BroadcastReceiver() {

    companion object {
        val TAG = "AH AudioOutputReceiver"

        val OUTPUT_TYPE = "output_type"

        val EXTERNAL_OUTPUT = 1242
        val INTERNAL_OUTPUT = 95423
        val BLUETOOTH_OUTPUT = 98765


        fun isMusicExternal(audioManager: AudioManager) : Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                val priority = listOf<Int>(
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                        AudioDeviceInfo.TYPE_AUX_LINE,
                        AudioDeviceInfo.TYPE_USB_DEVICE,
                        AudioDeviceInfo.TYPE_USB_ACCESSORY,
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
                        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                        AudioDeviceInfo.TYPE_TELEPHONY
                )

                var device : AudioDeviceInfo? = null
                var inferred = Int.MAX_VALUE

                for (o in audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                    Log.d(TAG, "Device: ${o.productName}")

                    if (priority.indexOf(o.type) == -1) {
                        if (inferred == Int.MAX_VALUE) {
                            device = device
                        }
                    } else if (priority.indexOf(o.type) < inferred) {
                        inferred = o.type
                        device = o
                    }
                }

                if (device != null) {
                    Log.d(TAG,"Output Device: ${device.productName}")
                }
                return inferred

            } else {
                Log.d(TAG, "Old output mechanism")
                // deprecated but still allowed if only using as a test.
                if (audioManager.isWiredHeadsetOn) {
                    Log.d(TAG,"External")
                    return EXTERNAL_OUTPUT
                } else if (audioManager.isBluetoothScoOn || audioManager.isBluetoothA2dpOn) {
                    Log.d(TAG,"Bluetooth")
                    return BLUETOOTH_OUTPUT
                } else {
                    Log.d(TAG,"Internal")
                    return INTERNAL_OUTPUT
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // some audio ouput has changed state

        Log.d(TAG, intent.action)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        var headphones = 0

        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            headphones = INTERNAL_OUTPUT
            Log.d(TAG, "Internal as noisy")
        } else if (intent.action == AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED) {
            if (intent.extras[AudioManager.EXTRA_SCO_AUDIO_STATE] !=
                    intent.extras[AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE]) {
                if (intent.extras[AudioManager.EXTRA_SCO_AUDIO_STATE] == AudioManager.SCO_AUDIO_STATE_CONNECTED
                        || intent.extras[AudioManager.EXTRA_SCO_AUDIO_STATE] == AudioManager.SCO_AUDIO_STATE_DISCONNECTED)
                    headphones = isMusicExternal(audioManager)
            }
        } else if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
            //TODO print extras
//            if (intent.getIntExtra(AudioManager.EXTRA_AUDIO_PLUG_STATE, 0) == 0) {
//                headphones = INTERNAL_OUTPUT
//            } else {
//                headphones = EXTERNAL_OUTPUT
//            }
        } else {
            headphones = isMusicExternal(audioManager)
        }

        val i = Intent(context, BackgroundService::class.java)

        i.putExtra(BackgroundService.STATE_CHANGE_EXTRA, BackgroundService.OUTPUT_CHANGED)
        Log.d(TAG, headphones.toString())
        i.putExtra(OUTPUT_TYPE, headphones)
        context.startService(i)
    }
}
