package com.angcyo.haloprogressbardemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val haloProgressBar: HaloProgressBar = findViewById(R.id.progress_bar)

        findViewById<View>(R.id.button).setOnClickListener {
            haloProgressBar.startHaloAnimator()
        }

        findViewById<SeekBar>(R.id.seek_bar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                haloProgressBar.progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
}
