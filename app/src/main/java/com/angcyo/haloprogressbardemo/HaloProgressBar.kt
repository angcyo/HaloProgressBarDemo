package com.angcyo.haloprogressbardemo

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View

/**
 * Created by angcyo on 2017-11-18.
 *
 * 模仿QQ 发送图片的 光晕进度条
 */
class HaloProgressBar(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    init {
        //context.obtainStyledAttributes()
    }

    public val View.density: Float
        get() = resources.displayMetrics.density

    public fun View.getColor(id: Int): Int = ContextCompat.getColor(context, id)

    /**返回居中绘制文本的y坐标*/
    public fun View.getDrawCenterTextCy(paint: Paint): Float {
        val rawHeight = measuredHeight - paddingTop - paddingBottom
        return paddingTop + rawHeight / 2 + (paint.descent() - paint.ascent()) / 2 - paint.descent()
    }

    public fun View.getDrawCenterTextCx(paint: Paint, text: String): Float {
        val rawWidth = measuredWidth - paddingLeft - paddingRight
        return paddingLeft + rawWidth / 2 - paint.measureText(text) / 2
    }

    companion object {
        fun getTranColor(@ColorInt color: Int, alpha: Int): Int {
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        }

        //勾股定理
        fun c(a: Float, b: Float): Float {
            return Math.sqrt((a * a + b * b).toDouble()).toFloat()
        }
    }


    private var circleCanvas: Canvas? = null
    private var circleBitmap: Bitmap? = null
    private var circleHaloCanvas: Canvas? = null
    private var circleHaloBitmap: Bitmap? = null

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    private val clearXF by lazy { PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val dstOutXF by lazy { PorterDuffXfermode(PorterDuff.Mode.DST_OUT) }
    private val srcOutXF by lazy { PorterDuffXfermode(PorterDuff.Mode.SRC_OUT) }

    /**内圈半径*/
    var circleInnerRadius = 16 * density
    /**外圈半径*/
    var circleOuterRadius = 18 * density

    var circleBgColor = getColor(R.color.transparent_dark60)

    private val circleHaloInnerRadius: Float
        get() {
            return circleInnerRadius - 1 * density
        }
    private val circleHaloOuterRadius: Float
        get() {
            return circleOuterRadius + 1 * density
        }

    /**圆圈颜色*/
    var circleColor = Color.WHITE
    /**光晕圆圈颜色*/
    var circleHaloColor = getTranColor(Color.WHITE, 0x30)

    /**动画时, 外圈允许的最大半径*/
    private val circleOuterAnimMaxRadius: Float
        get() {
            return circleOuterRadius + 2 * density
        }

    private val circleHaloInnerAnimMiniRadius: Float
        get() {
            return circleHaloInnerRadius - 4 * density
        }

    private val circleHaloOuterAnimMaxRadius: Float
        get() {
            return circleHaloOuterRadius + 4 * density
        }

    /**进度 0 - 100*/
    var progress = 0
        set(value) {
            field = value
            if (field >= 100) {
                startHaloFinishAnimator()
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (progress < 100) {
            if (animator.isStarted) {
                //动画开始了, 才绘制

                //背景色
                canvas.drawColor(circleBgColor)

                //绘制光晕
                circleHaloCanvas?.let {
                    it.save()
                    it.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())

                    paint.xfermode = clearXF
                    it.drawPaint(paint)
                    paint.xfermode = null

                    //绘制光晕
                    paint.color = circleHaloColor
                    it.drawCircle(0f, 0f, circleHaloOuterRadius + (circleHaloOuterAnimMaxRadius - circleHaloOuterRadius) * animatorValue, paint)

                    paint.xfermode = srcOutXF
                    it.drawCircle(0f, 0f, circleHaloInnerRadius - (circleHaloInnerRadius - circleHaloInnerAnimMiniRadius) * animatorValue, paint)
                    paint.xfermode = null

                    it.restore()

                    canvas.drawBitmap(circleHaloBitmap, 0f, 0f, null)
                }
                //绘制圆
                circleCanvas?.let {
                    it.save()
                    it.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())

                    paint.xfermode = clearXF
                    it.drawPaint(paint)
                    paint.xfermode = null

                    //绘制圆圈
                    paint.color = circleColor
                    it.drawCircle(0f, 0f, circleOuterRadius + (circleOuterAnimMaxRadius - circleOuterRadius) * animatorValue, paint)

                    paint.xfermode = srcOutXF
                    it.drawCircle(0f, 0f, circleInnerRadius, paint)
                    paint.xfermode = null

                    it.restore()

                    canvas.drawBitmap(circleBitmap, 0f, 0f, null)

                }

                if (progress > 0) {
                    //绘制进度文本
                    paint.apply {
                        paint.style = Paint.Style.FILL_AND_STROKE
                        paint.textSize = 12 * density
                        paint.color = circleColor
                        paint.strokeWidth = 1f
                    }
                    val text = "${progress}%"
                    canvas.drawText(text,
                            this.getDrawCenterTextCx(paint, text),
                            this.getDrawCenterTextCy(paint),
                            paint)
                }
            } else {
                //动画没开始, 不绘制
            }
        } else if (animatorFinish.isRunning) {
            circleCanvas?.let {
                paint.xfermode = clearXF
                it.drawPaint(paint)
                paint.xfermode = null

                //绘制圆圈
                paint.color = circleBgColor
                it.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)

                it.save()
                it.translate((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())

                paint.xfermode = srcOutXF
                paint.color = Color.TRANSPARENT
                it.drawCircle(0f, 0f, circleFinishDrawRadius, paint)
                paint.xfermode = null

                it.restore()

                canvas.drawBitmap(circleBitmap, 0f, 0f, null)
            }
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circleBitmap?.recycle()
        if (measuredWidth != 0 && measuredHeight != 0) {
            circleBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            circleCanvas = Canvas(circleBitmap)
        }

        circleHaloBitmap?.recycle()
        if (measuredWidth != 0 && measuredHeight != 0) {
            circleHaloBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            circleHaloCanvas = Canvas(circleHaloBitmap)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (circleBitmap != null && circleBitmap!!.isRecycled) {
            circleBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            circleCanvas = Canvas(circleBitmap)
        }
        if (circleHaloBitmap != null && circleHaloBitmap!!.isRecycled) {
            circleHaloBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            circleHaloCanvas = Canvas(circleHaloBitmap)
        }
        //startHaloAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopHaloAnimator()
        stopHaloFinishAnimator()
        circleHaloBitmap?.recycle()
        circleBitmap?.recycle()
        progress = 0
    }

    /*动画进度*/
    private var animatorValue: Float = 0f

    private val animator by lazy {
        ObjectAnimator.ofFloat(0f, 1f).apply {
            duration = 700
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                animatorValue = it.animatedValue as Float
//                progress++
//                if (progress > 100) {
//                    progress = 0
//                }
                postInvalidate()
            }
        }
    }

    /**启动光晕动画*/
    fun startHaloAnimator() {
        if (progress >= 100) {
            return
        }
        if (animator.isStarted || animator.isRunning) {
        } else {
            animator.start()
        }
    }

    fun stopHaloAnimator() {
        animator.cancel()
    }

    /**结束动画圆圈的半径*/
    private var circleFinishDrawRadius = circleInnerRadius

    private val animatorFinish by lazy {
        ObjectAnimator.ofFloat(circleInnerRadius, c((measuredWidth / 2).toFloat(), (measuredHeight / 2).toFloat())).apply {
            duration = 300
            addUpdateListener {
                circleFinishDrawRadius = it.animatedValue as Float
                postInvalidate()
            }
        }
    }

    /**进度100%后的动画*/
    fun startHaloFinishAnimator() {
        stopHaloAnimator()
        if (animatorFinish.isStarted || animatorFinish.isRunning) {
        } else {
            animatorFinish.start()
        }
    }

    fun stopHaloFinishAnimator() {
        animatorFinish.cancel()
    }
}
