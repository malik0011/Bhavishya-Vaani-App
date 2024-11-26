package com.example.astrochart

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Start the rotation animation
        val splashLogo = findViewById<ImageView>(R.id.splashLogo)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
        splashLogo.startAnimation(rotateAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
} 