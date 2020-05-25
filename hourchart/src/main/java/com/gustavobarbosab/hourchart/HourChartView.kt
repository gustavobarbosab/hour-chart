package com.gustavobarbosab.hourchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlinx.android.parcel.Parcelize
import java.lang.IllegalArgumentException

class HourChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var chartRect = RectF()

    private val workedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val missingPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var totalHours = 160
    private var workedHours = 0

    var strokeWidth = STROKE_WIDTH_DEFAULT
        set(value) {
            field = value
            workedPaint.strokeWidth = value
            missingPaint.strokeWidth = value
            invalidate()
        }
    var workedColorResource = Color.GREEN
        set(value) {
            field = value
            workedPaint.color = value
            invalidate()
        }
    var missingColorResource = Color.RED
        set(value) {
            field = value
            missingPaint.color = value
            invalidate()
        }

    init {
        workedPaint.apply {
            color = workedColorResource
            style = Paint.Style.STROKE
            strokeWidth = this@HourChartView.strokeWidth
            strokeCap = Paint.Cap.BUTT
        }
        missingPaint.apply {
            color = missingColorResource
            style = Paint.Style.STROKE
            strokeWidth = this@HourChartView.strokeWidth
            strokeCap = Paint.Cap.BUTT
        }
    }

    override fun onSaveInstanceState(): Parcelable? =
        ViewState(
            strokeWidth,
            workedColorResource,
            missingColorResource,
            totalHours,
            workedHours,
            super.onSaveInstanceState()
        )


    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = state
        if (state is ViewState) {
            strokeWidth = state.strokeWidth
            workedColorResource = state.workedColorResource
            missingColorResource = state.missingColorResource
            totalHours = state.totalHours
            workedHours = state.workedHours
            workedHours = state.workedHours
            superState = state.superState
        }
        super.onRestoreInstanceState(superState)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawMissingHours(canvas)
        drawWorkedHours(canvas)
    }

    private fun drawWorkedHours(canvas: Canvas) {
        val workedHoursAngle = convertHoursToAngle(workedHours)
        if (workedHoursAngle == 0f) return
        canvas.drawArc(chartRect, 0f, workedHoursAngle, false, workedPaint)
    }

    private fun drawMissingHours(canvas: Canvas) {
        canvas.drawArc(chartRect, 0f, 360f, false, missingPaint)
    }

    private fun convertHoursToAngle(hours: Int): Float = (hours * 360f) / totalHours

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        changeChartRect(w, h)
    }

    private fun changeChartRect(w: Int, h: Int) {
        val start = paddingStart + PADDING_DEFAULT
        val top = paddingTop + PADDING_DEFAULT
        val end = w.toFloat() - (paddingEnd + PADDING_DEFAULT)
        val bottom = h.toFloat() - (paddingBottom + PADDING_DEFAULT)

        chartRect.set(start, top, end, bottom)
    }

    fun setup(totalHours: Int, workedHours: Int) {
        if (workedHours > totalHours) throw IllegalArgumentException()
        this.totalHours = totalHours
        this.workedHours = workedHours
        invalidate()
    }

    @Parcelize
    data class ViewState(
        val strokeWidth: Float,
        val workedColorResource: Int,
        val missingColorResource: Int,
        val totalHours: Int,
        val workedHours: Int,
        val superState: Parcelable?
    ) : Parcelable

    companion object {
        const val PADDING_DEFAULT = 60F
        const val STROKE_WIDTH_DEFAULT = 60F
    }
}