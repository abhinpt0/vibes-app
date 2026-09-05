package com.vibes.dsrapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.vibes.dsrapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dateStr = getSharedPreferences("dsr_prefs", MODE_PRIVATE)
            .getString("dsr_date", "") ?: ""

        // Format title bar: "DSR — 05 Sep"
        if (dateStr.isNotEmpty()) {
            runCatching {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                val label = SimpleDateFormat("dd MMM", Locale.getDefault()).format(parsed!!)
                supportActionBar?.title = "DSR — $label"
            }
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard,
                R.id.nav_retailers,
                R.id.nav_credit,
                R.id.nav_debit,
                R.id.nav_remarks
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)
    }
}
