package ms.jada.canyouhearthemusic.canyouhearthemusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
class ScreenStateReceiver : BroadcastReceiver() {

    companion object {
        val TAG = "AH ScreenStateReceiver"

        val SCREEN_CHANGE = "screen_change"
        val SCREEN_OFF = 0
        val SCREEN_ON = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        var state : Int = -1
        if (intent.action.equals(Intent.ACTION_SCREEN_ON)) {
            Log.i(TAG, "Screen ON")
            state = SCREEN_ON
        }
        else if (intent.action.equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(TAG, "Screen OFF")
            state = SCREEN_OFF
        }

        val i = Intent(context, BackgroundService::class.java)

        i.putExtra(BackgroundService.STATE_CHANGE_EXTRA, BackgroundService.SCREEN_CHANGED)
        i.putExtra(SCREEN_CHANGE, state)

        context.startService(i)
    }
}
