package com.cleon.lokerku.awan.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cleon.lokerku.awan.MainActivity
import com.cleon.lokerku.awan.R
import com.cleon.lokerku.awan.databinding.ActivitySplashBinding
import com.cleon.lokerku.awan.helper.preference.UserPreference
import com.cleon.lokerku.awan.ui.auth.LoginActivity
import com.cleon.lokerku.awan.ui.dashboard.DashboardActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale)
        binding.logoImageView.startAnimation(rotateAnimation)
        binding.logoImageView.startAnimation(scaleAnimation)

        Handler().postDelayed({
            val userPreference = UserPreference(this)
            val username = userPreference.getUsername()
            if (username == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000) // 3000 ms = 3 detik
    }
}