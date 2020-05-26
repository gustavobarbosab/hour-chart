package com.gustavobarbosab.hourchart

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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

    private var textLegendSize = MIN_TEXT_SIZE
    val textSizePx
        get() = if (textLegendSize >= MIN_TEXT_SIZE) toPx(textLegendSize) else toPx(MIN_TEXT_SIZE)
    private var circleLegendSize = LEGEND_DOT_SIZE
    private var strokeWidth = STROKE_WIDTH_DEFAULT
        set(value) {
            field = value
            workedPaint.strokeWidth = strokeWidthPx
            missingPaint.strokeWidth = strokeWidthPx
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

        textPaint.textSize = textSizePx
        val halfTextSize = textSizePx / 2

        val workedY = centerY - halfTextSize
        val missingY = centerY + textSizePx
        val marginEndCircleToText = toPx(4f)
        val circleSize = toPx(circleLegendSize)

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
            centerX - ((textBounds.width() / 2) + circleSize + marginEndCircleToText),
            workedY + textBounds.exactCenterY(),
            circleSize,
            circleLegendPaint
        )

        // Draw missing legend
        val missingString = "-${missingHours}h"
        canvas.drawText(missingString, centerX, missingY, textPaint)
        textPaint.getTextBounds(missingString, 0, missingString.length, textBounds)
        circleLegendPaint.color = missingColorResource
        canvas.drawCircle(
            centerX - ((textBounds.width() / 2) + circleSize + marginEndCircleToText),
            missingY + textBounds.exactCenterY(),
            circleSize,
            circleLegendPaint
        )
    }

    private fun convertHoursToAngle(hours: Int): Float = (hours * 360f) / totalHours

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val paddingPx = toPx(PADDING_DEFAULT)
        val startChart = paddingStart + paddingPx
        val topChart = paddingTop + paddingPx
        val endChart = w.toFloat() - (paddingEnd + paddingPx)
        val bottomChart = h.toFloat() - (paddingBottom + paddingPx)
        chartRect.set(startChart, topChart, endChart, bottomChart)

        // Calculate chart sizes
        textLegendSize = (chartRect.height() * 0.04).toFloat()
        circleLegendSize = (chartRect.height() * 0.01).toFloat()
        strokeWidth = (chartRect.height() * 0.04).toFloat()
    }

    fun setWorkedHours(workedHours: Int) {
        if (workedHours > totalHours) {
            throw IllegalArgumentException()
        }
        this.workedHours = workedHours
        invalidate()
    }

    fun setTotalHours(totalHours: Int) {
        if (workedHours > totalHours) {
            throw IllegalArgumentException()
        }
        this.totalHours = totalHours
        invalidate()
    }

    fun setWorkedHoursAnimated(workedHours: Int, durationAnimation: Long = 3000) {
        ObjectAnimator.ofInt(1, workedHours).apply {
            duration = durationAnimation
            addUpdateListener { anim ->
                setWorkedHours((anim.animatedValue as Int))
            }
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun toPx(dp: Float) = dp * context.resources.displayMetrics.density

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
        const val PADDING_DEFAULT = 18F
        const val STROKE_WIDTH_DEFAULT = 18F
        const val LEGEND_DOT_SIZE = 6F
        const val MIN_TEXT_SIZE = 12F
    }
}