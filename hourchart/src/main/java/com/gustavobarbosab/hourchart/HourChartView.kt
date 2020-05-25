package com.gustavobarbosab.hourchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlinx.android.parcel.Parcelize

class HourChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var chartRect = RectF()
    private var textBounds = Rect()

    private val workedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val missingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circleLegendPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var totalHours = TOTAL_HOURS_DEFAULT
    private var workedHours = 0
    private val missingHours
        get() = totalHours - workedHours

    var strokeWidth = STROKE_WIDTH_DEFAULT
        set(value) {
            field = value
            workedPaint.strokeWidth = strokeWidthPx
            missingPaint.strokeWidth = strokeWidthPx
            invalidate()
        }
    private val strokeWidthPx
        get() = toPx(strokeWidth)

    var workedColorResource = 0xff54c47f.toInt()
        set(value) {
            field = value
            workedPaint.color = value
            invalidate()
        }

    var missingColorResource = 0xFFEB9191.toInt()
        set(value) {
            field = value
            missingPaint.color = value
            invalidate()
        }

    init {
        workedPaint.apply {
            color = workedColorResource
            style = Paint.Style.STROKE
            strokeWidth = this@HourChartView.strokeWidthPx
            strokeCap = Paint.Cap.BUTT
        }
        missingPaint.apply {
            color = missingColorResource
            style = Paint.Style.STROKE
            strokeWidth = this@HourChartView.strokeWidthPx
            strokeCap = Paint.Cap.BUTT
        }
        textPaint.apply {
            color = Color.DKGRAY
            textSize = toPx(TEXT_SIZE_DEFAULT)
            textAlign = Paint.Align.CENTER
        }
        circleLegendPaint.style = Paint.Style.FILL
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
        drawLegend(canvas)
    }

    private fun drawWorkedHours(canvas: Canvas) {
        val workedHoursAngle = convertHoursToAngle(workedHours)
        if (workedHoursAngle == 0f) return
        canvas.drawArc(chartRect, 0f, workedHoursAngle, false, workedPaint)
    }

    private fun drawMissingHours(canvas: Canvas) {
        canvas.drawArc(chartRect, 0f, 360f, false, missingPaint)
    }

    private fun drawLegend(canvas: Canvas) {
        val centerX = chartRect.centerX()
        val centerY = chartRect.centerY()

        val textSize = toPx(TEXT_SIZE_DEFAULT)
        val halfTextSize = textSize / 2

        val workedY = centerY - halfTextSize
        val missingY = centerY + textSize
        val marginEndCircleToText = toPx(4f)

        // Draw worked legend
        val workedString = "+${workedHours}h"
        canvas.drawText(
            workedString,
            centerX,
            workedY,
            textPaint
        )
        textPaint.getTextBounds(workedString, 0, workedString.length, textBounds)
        circleLegendPaint.color = workedColorResource
        canvas.drawCircle(
            centerX - ((textBounds.width() / 2) + halfTextSize + marginEndCircleToText),
            workedY + textBounds.exactCenterY(),
            halfTextSize,
            circleLegendPaint
        )

        // Draw missing legend
        val missingString = "-${missingHours}h"
        canvas.drawText(missingString, centerX, missingY, textPaint)
        textPaint.getTextBounds(missingString, 0, missingString.length, textBounds)
        circleLegendPaint.color = missingColorResource
        canvas.drawCircle(
            centerX - ((textBounds.width() / 2) + halfTextSize + marginEndCircleToText),
            missingY + textBounds.exactCenterY(),
            halfTextSize,
            circleLegendPaint
        )
    }

    private fun convertHoursToAngle(hours: Int): Float = (hours * 360f) / totalHours

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val startChart = paddingStart + PADDING_DEFAULT
        val topChart = paddingTop + PADDING_DEFAULT
        val endChart = w.toFloat() - (paddingEnd + PADDING_DEFAULT)
        val bottomChart = h.toFloat() - (paddingBottom + PADDING_DEFAULT)
        chartRect.set(startChart, topChart, endChart, bottomChart)
    }

    fun setup(workedHours: Int, totalHours: Int = TOTAL_HOURS_DEFAULT) {
        if (workedHours > totalHours) throw IllegalArgumentException()
        this.totalHours = totalHours
        this.workedHours = workedHours
        invalidate()
    }

    private fun toPx(dp: Float) = dp * context.resources.displayMetrics.density

    private fun toDp(px: Float) = px / context.resources.displayMetrics.density

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
        const val TOTAL_HOURS_DEFAULT = 200
        const val PADDING_DEFAULT = 60F
        const val STROKE_WIDTH_DEFAULT = 22F
        const val TEXT_SIZE_DEFAULT = 16F
    }
}