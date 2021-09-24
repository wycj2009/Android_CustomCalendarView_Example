package com.example.android_customcalendarview_example

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainCalendarviewCalendar: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityMainCalendarviewCalendar = findViewById(R.id.activity_main_calendarview_calendar)

        activityMainCalendarviewCalendar.setOnDateClickListener { cell, date ->
            Log.d("myLog", "$date")
        }
    }

}