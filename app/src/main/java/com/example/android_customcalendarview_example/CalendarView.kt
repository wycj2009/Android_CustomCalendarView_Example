package com.example.android_customcalendarview_example

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/** 캘린더뷰 */
class CalendarView : ConstraintLayout {

    companion object {
        private const val MAX_PAGE_SIZE = 2401 //달력 페이지 개수
        private const val CENTER_PAGE_NUM = MAX_PAGE_SIZE / 2 //가운데 페이지 숫자
    }

    private lateinit var viewCalendarImagebuttonLeftPage: ImageButton //이전 달로 이동 버튼
    private lateinit var viewCalendarTextviewMonth: TextView //타이틀 년월
    private lateinit var viewCalendarImagebuttonRightPage: ImageButton //다음 달로 이동 버튼
    private lateinit var viewCalendarRecyclerviewPagingArea: RecyclerView //페이징 구역

    private var selectedDate: LocalDate? = null //선택된 날짜
    private var selectedCell: View? = null //선택된 셀
    private var dateClickAction: (cell: View, date: LocalDate) -> Unit = { view: View, localDate: LocalDate -> } //날짜 클릭 액션
    private var curPageNum = CENTER_PAGE_NUM //현재 페이지 숫자
    private var cellHeight: Int? = null //셀 높이

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /** 캘린더뷰 초기화 */
    private fun init() {
        View.inflate(context, R.layout.view_calendar, this) //캘린더뷰 인플레이트

        viewCalendarImagebuttonLeftPage = findViewById(R.id.view_calendar_imagebutton_left_page)
        viewCalendarTextviewMonth = findViewById(R.id.view_calendar_textview_month)
        viewCalendarImagebuttonRightPage = findViewById(R.id.view_calendar_imagebutton_right_page)
        viewCalendarRecyclerviewPagingArea = findViewById(R.id.view_calendar_recyclerview_paging_area)

        val firstDayOfThisMonth = LocalDate.now().withDayOfMonth(1) //현재 월의 첫 날짜
        val pagerSnapHelper = PagerSnapHelper() //페이징 기능을 위해 선언
        val linearLayoutManager = viewCalendarRecyclerviewPagingArea.layoutManager as LinearLayoutManager //페이징 리사이클러뷰의 LinearLayoutManager

        viewCalendarTextviewMonth.text = "${getDateWithBestPattern(firstDayOfThisMonth.plusMonths((curPageNum - CENTER_PAGE_NUM).toLong()), "yyyyM")}" //타이틀 년월 세팅

        viewCalendarRecyclerviewPagingArea.run {
            pagerSnapHelper.attachToRecyclerView(this) //페이징 기능 세팅

            //스크롤 리스너 세팅
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val snapView = pagerSnapHelper.findSnapView(linearLayoutManager)!! //PagerSnapHelper 가 선택한 뷰
                    val snapPosition = linearLayoutManager.getPosition(snapView) //PagerSnapHelper 가 선택한 뷰의 position

                    if (curPageNum != snapPosition) { //새로 선택된 페이지가 이전 페이지와 다른 경우
                        /*
                        리사이클러뷰의 ScrollState 에 따라 알맞은 기능을 수행한다
                        ScrollState 가 SCROLL_STATE_SETTLING 이라면 스크롤 해서 달을 이동한 경우이다
                        ScrollState 가 SCROLL_STATE_IDLE 이라면 버튼을 통하여 달을 이동한 경우이다
                         */
                        when (viewCalendarRecyclerviewPagingArea.scrollState) {
                            RecyclerView.SCROLL_STATE_SETTLING -> { //스크롤 해서 달을 이동한 경우
                                viewCalendarTextviewMonth.text = "${getDateWithBestPattern(firstDayOfThisMonth.plusMonths((snapPosition - CENTER_PAGE_NUM).toLong()), "yyyyM")}" //타이틀 년월 세팅
                                curPageNum = snapPosition //새로 선택된 페이지숫자를 세팅
                            }
                            RecyclerView.SCROLL_STATE_IDLE -> { //버튼을 통하여 달을 이동한 경우
                                viewCalendarTextviewMonth.text = "${getDateWithBestPattern(firstDayOfThisMonth.plusMonths((snapPosition - CENTER_PAGE_NUM).toLong()), "yyyyM")}" //타이틀 년월 세팅

                                //애니메이션 적용
                                val animationSet = AnimationSet(true).apply {
                                    //알파 애니메이션
                                    val alphaAnimation = AlphaAnimation(0.0f, 1.0f).apply {
                                        duration = 350
                                        interpolator = DecelerateInterpolator()
                                    }
                                    //이동 애니메이션
                                    val translateAnimation = if (snapPosition < curPageNum) { //이전 달로 이동한 경우
                                        TranslateAnimation(-85.0f, 0.0f, 0.0f, 0.0f).apply {
                                            duration = 250
                                            interpolator = DecelerateInterpolator()
                                        }
                                    } else { //다음 달로 이동한 경우
                                        TranslateAnimation(85.0f, 0.0f, 0.0f, 0.0f).apply {
                                            duration = 250
                                            interpolator = DecelerateInterpolator()
                                        }
                                    }

                                    addAnimation(alphaAnimation) //알파 애니메이션 추가
                                    addAnimation(translateAnimation) //이동 애니메이션 추가
                                }
                                snapView.startAnimation(animationSet) //애니메이션 시작

                                curPageNum = snapPosition //새로 선택된 페이지숫자를 세팅
                            }
                        }
                    }
                }
            })

            adapter = PageAdapter(firstDayOfThisMonth) //페이지 어댑터 세팅
        }

        //이전 달로 이동 버튼 리스너 세팅
        viewCalendarImagebuttonLeftPage.setOnClickListener {
            val nextPageNum = curPageNum - 1
            if (nextPageNum in 0..MAX_PAGE_SIZE) viewCalendarRecyclerviewPagingArea.scrollToPosition(nextPageNum)
        }

        //다음 달로 이동 버튼 리스너 세팅
        viewCalendarImagebuttonRightPage.setOnClickListener {
            val nextPageNum = curPageNum + 1
            if (nextPageNum in 0..MAX_PAGE_SIZE) viewCalendarRecyclerviewPagingArea.scrollToPosition(nextPageNum)
        }
    }

    /** 알맞은 형식의 날짜 가져오기 */
    private fun getDateWithBestPattern(localDate: LocalDate, pattern: String): String {
        val bestDateTimePattern: String = DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern)
        return localDate.format(DateTimeFormatter.ofPattern(bestDateTimePattern))
    }

    /** 날짜 선택 */
    private fun selectDate(date: LocalDate, cell: View) {
        selectedDate = date //선택된 날짜 세팅

        //선택된 셀 세팅
        selectedCell?.isSelected = false
        cell.isSelected = true
        selectedCell = cell
    }

    /** 날짜 클릭 리스너 세팅 */
    fun setOnDateClickListener(dateClickAction: (cell: View, date: LocalDate) -> Unit) {
        this.dateClickAction = dateClickAction
    }

    /** 페이지 어댑터 */
    inner class PageAdapter(private val firstDayOfThisMonth: LocalDate) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val pageList = List(MAX_PAGE_SIZE) { it } //페이지 숫자 리스트
        private val gridAdapter = GridAdapter() //그리드 어댑터

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //달력의 그리드 부분을 구현하기 위한 리사이클러뷰를 추가
            val recyclerView = RecyclerView(parent.context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                layoutManager = object : GridLayoutManager(context, 7) {
                    override fun canScrollHorizontally(): Boolean = false //좌우 스크롤 방지
                    override fun canScrollVertically(): Boolean = false //상하 스크롤 방지
                }
            }

            return PageViewHolder(recyclerView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as PageViewHolder).setItem(pageList[position])
        }

        override fun getItemCount(): Int {
            return pageList.size
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            recyclerView.scrollToPosition(curPageNum)
        }

        /** 페이지 뷰홀더 */
        inner class PageViewHolder(private val recyclerView: RecyclerView) : RecyclerView.ViewHolder(recyclerView) {

            fun setItem(position: Int) {
                gridAdapter.updateCellInfoList(firstDayOfThisMonth.plusMonths((position - CENTER_PAGE_NUM).toLong())) //셀 정보 리스트 업데이트

                if (cellHeight == null) {
                    recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            cellHeight = recyclerView.height / 7
                            recyclerView.adapter = gridAdapter
                        }
                    })
                } else {
                    recyclerView.adapter = gridAdapter
                }
            }

        }

    }

    /** 그리드 어댑터 */
    inner class GridAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val cellInfoList = MutableList<LocalDate?>(49) { null }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_cell, parent, false)
            return CellViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as CellViewHolder).setItem(position)
        }

        override fun getItemCount(): Int {
            return cellInfoList.size
        }

        /** 셀 정보 리스트 업데이트 */
        fun updateCellInfoList(firstDayOfCurMonth: LocalDate) {
            cellInfoList.forEachIndexed { index, localDate ->
                cellInfoList[index] = null
            }

            val firstIdx = ((firstDayOfCurMonth.dayOfWeek.value + 7) % 7) + 7
            val lastIdx = firstIdx + firstDayOfCurMonth.lengthOfMonth() - 1

            var day = 0L
            for (i in firstIdx..lastIdx) {
                cellInfoList[i] = firstDayOfCurMonth.plusDays(day)
                day++
            }
        }

        /** 셀 뷰홀더 */
        inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val itemCalendarTextviewContent: TextView = itemView.findViewById(R.id.item_calendar_textview_content)

            private var item: LocalDate? = null

            init {
                itemView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, cellHeight!!)

                itemView.setOnClickListener {
                    if (item != null) {
                        if (item!! != selectedDate) selectDate(item!!, it) //날짜 선택

                        dateClickAction(it, item!!) //날짜 클릭 액션 실행
                    }
                }
            }

            fun setItem(position: Int) {
                this.item = cellInfoList[position]

                //셀 컨텐츠 세팅
                when (position) {
                    0 -> itemCalendarTextviewContent.text = DayOfWeek.SUNDAY.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    1 -> itemCalendarTextviewContent.text = DayOfWeek.MONDAY.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    2 -> itemCalendarTextviewContent.text = DayOfWeek.TUESDAY.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    3 -> itemCalendarTextviewContent.text = DayOfWeek.WEDNESDAY.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    4 -> itemCalendarTextviewContent.text = DayOfWeek.THURSDAY.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    5 -> itemCalendarTextviewContent.text = DayOfWeek.FRIDAY.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    6 -> itemCalendarTextviewContent.text = DayOfWeek.SATURDAY.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    else -> if (item != null) {
                        if (selectedDate != null && selectedDate == item) {
                            selectDate(item!!, itemView) //날짜 선택
                        }

                        itemCalendarTextviewContent.text = "${item!!.dayOfMonth}"

                        itemView.setBackgroundResource(R.drawable.selector_cell)
                    } else {
                        itemCalendarTextviewContent.text = ""
                    }
                }

                //셀 컨텐츠 색상 세팅
                when (position % 7) {
                    0 -> itemCalendarTextviewContent.setTextColor(ContextCompat.getColor(context, R.color.sunday))
                    6 -> itemCalendarTextviewContent.setTextColor(ContextCompat.getColor(context, R.color.saturday))
                }
            }

        }

    }

}