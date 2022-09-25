package com.example.android_customcalendarview_example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android_customcalendarview_example.CalendarView.Companion.toStringWithBestPattern
import com.example.android_customcalendarview_example.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calendar.run {
            // 달력 페이지 변경 콜백 세팅
            pageChangedCallback = { currentPageFirstDayOfMonth: LocalDate ->
                binding.currentPageMonth.text = currentPageFirstDayOfMonth.toStringWithBestPattern("yyyyM")
            }
            // 달력 날짜 선택 콜백 세팅
            dateSelectedCallback = { selectedDate: LocalDate ->
                Snackbar.make(binding.root, "$selectedDate", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.goToPastPage.setOnClickListener {
            binding.calendar.goToPastPage() // 과거 페이지로 이동
        }

        binding.goToFuturePage.setOnClickListener {
            binding.calendar.goToFuturePage() // 미래 페이지로 이동
        }
    }
}
