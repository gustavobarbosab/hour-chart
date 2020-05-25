package com.gustavobarbosab.hourchart

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.btChangeChart
import kotlinx.android.synthetic.main.activity_main.hourChart
import java.util.Random

class MainActivity : AppCompatActivity() {

    private val rnd = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hourChart.setup(20)
        btChangeChart.setOnClickListener {
            hourChart.workedColorResource = randomColor()
            hourChart.missingColorResource = randomColor()
            hourChart.setup(rnd.nextInt(199))
        }
    }

    private fun randomColor() = Color.argb(
        255,
        rnd.nextInt(256),
        rnd.nextInt(256),
        rnd.nextInt(256)
    )
}
