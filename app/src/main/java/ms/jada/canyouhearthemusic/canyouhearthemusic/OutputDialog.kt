package ms.jada.canyouhearthemusic.canyouhearthemusic

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NotificationManagerCompat
import android.view.Window
import kotlinx.android.synthetic.main.activity_output_dialog.*

class OutputDialog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_output_dialog)

        title = ""
        supportActionBar?.hide()

        on_ear.setOnClickListener {
            finish()
        }

        in_ear.setOnClickListener {
            finish()
        }

        over_ear.setOnClickListener {
            finish()

        }

        speakers.setOnClickListener {
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(BackgroundService.OUTPUT_NOTIFICATION_ID)
    }
}
