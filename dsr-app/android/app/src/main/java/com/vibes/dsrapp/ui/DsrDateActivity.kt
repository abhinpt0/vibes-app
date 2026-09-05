package com.vibes.dsrapp.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vibes.dsrapp.MainActivity
import com.vibes.dsrapp.databinding.ActivityDsrDateBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DsrDateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDsrDateBinding
    private val cal = Calendar.getInstance()

    private val storeFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFmt = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDsrDateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        updateDateLabel()

        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(year, month, day)
                    updateDateLabel()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnStartDsr.setOnClickListener {
            val dateStr = storeFmt.format(cal.time)
            getSharedPreferences("dsr_prefs", MODE_PRIVATE)
                .edit()
                .putString("dsr_date", dateStr)
                .apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun updateDateLabel() {
        binding.tvSelectedDate.text = displayFmt.format(cal.time)
    }
}
