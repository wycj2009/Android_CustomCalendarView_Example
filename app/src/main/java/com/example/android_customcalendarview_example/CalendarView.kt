package com.example.android_customcalendarview_example

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.GridLayout.UNDEFINED
import android.widget.GridLayout.spec
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {
    var pageChangedCallback: ((currentPageFirstDayOfMonth: LocalDate) -> Unit)? = null
        set(value) {
            field = value
            field?.invoke(currentPageFirstDayOfMonth)
        }
    var dateSelectedCallback: ((selectedDate: LocalDate) -> Unit)? = null
    val today: LocalDate = LocalDate.now()
    var currentPageFirstDayOfMonth: LocalDate = today.withDayOfMonth(1)
        private set
    var selectedDate: LocalDate = today
        private set
    private var selectedCell: View? = null
    private val pagerAdapter = object : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val page = GridLayout(context).apply {
                columnCount = 7
                rowCount = 7

                // Add dayOfWeek cells
                listOf(
                    DayOfWeek.SUNDAY,
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY
                ).forEach {
                    addView(generateDayOfWeekCell(it))
                }

                // Add dayOfMonth cells
                val firstDayOfMonth = today.withDayOfMonth(1).plusMonths((position - CENTER_PAGE_INDEX).toLong())
                val firstCellIdx = (firstDayOfMonth.dayOfWeek.value + 7) % 7
                val lastCellIdx = firstCellIdx + firstDayOfMonth.lengthOfMonth() - 1
                for (i in 0 until 42) {
                    addView(generateDayOfMonthCell(if (i in firstCellIdx..lastCellIdx) firstDayOfMonth.plusDays((i - firstCellIdx).toLong()) else null))
                }
            }
            container.addView(page)
            return page
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount(): Int {
            return TOTAL_PAGE_SIZE
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        /** 요일 셀 생성 (커스텀 가능) */
        private fun generateDayOfWeekCell(dayOfWeek: DayOfWeek): View {
            return TextView(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    columnSpec = spec(UNDEFINED, 1f)
                    rowSpec = spec(UNDEFINED, 1f)
                    gravity = Gravity.CENTER
                }
                text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            }
        }

        /** 일 셀 생성 (커스텀 가능) */
        private fun generateDayOfMonthCell(dateOfCell: LocalDate?): View {
            return TextView(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    columnSpec = spec(UNDEFINED, 1f)
                    rowSpec = spec(UNDEFINED, 1f)
                    gravity = Gravity.CENTER
                }
                if (dateOfCell != null) {
                    text = "${dateOfCell.dayOfMonth}"
                    foreground = ContextCompat.getDrawable(context, R.drawable.effect_calendar_cell)
                    if (dateOfCell == selectedDate) {
                        isSelected = true
                        selectedCell = this
                    }
                    setOnClickListener {
                        if (dateOfCell != selectedDate) {
                            selectedDate = dateOfCell
                            selectedCell?.isSelected = false
                            it.isSelected = true
                            selectedCell = it
                        }
                        dateSelectedCallback?.invoke(selectedDate)
                    }
                }
            }
        }
    }
    private val onPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            currentPageFirstDayOfMonth = today.withDayOfMonth(1).plusMonths((position - CENTER_PAGE_INDEX).toLong())
            pageChangedCallback?.invoke(currentPageFirstDayOfMonth)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageScrollStateChanged(state: Int) {}
    }

    init {
        adapter = pagerAdapter
        currentItem = CENTER_PAGE_INDEX
        addOnPageChangeListener(onPageChangeListener)
    }

    /** 과거 페이지로 이동 */
    fun goToPastPage() {
        currentItem -= 1
    }

    /** 미래 페이지로 이동 */
    fun goToFuturePage() {
        currentItem += 1
    }

    companion object {
        private const val TOTAL_PAGE_SIZE = Int.MAX_VALUE // 총 페이지 개수
        private const val CENTER_PAGE_INDEX = TOTAL_PAGE_SIZE / 2 // 가운데 페이지 인덱스

        /** LocalDate -> String with best pattern  */
        fun LocalDate.toStringWithBestPattern(pattern: String): String {
            val bestDateTimePattern: String = DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern)
            return this.format(DateTimeFormatter.ofPattern(bestDateTimePattern))
        }
    }
}
