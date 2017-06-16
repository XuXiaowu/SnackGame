package fuckermonkey.snackgame.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import fuckermonkey.snackgame.ui.constant.Constants

/**
 * Created by xuxiaowu on 2017/6/14.
 */
class GridView : View {

    val GRID_LINE_WIDTH = 1
    val DEFAULT_MARGIN = 20

    var mWidth: Int? = null
    var mHeight: Int? = null
    var mGridWidth: Int? = null
    var mMargin: Int? = null
    var mGridNum = Constants.getInstance().SIMPLE_GRID_NUM

    var mGridLinePaint: Paint? = null //栅格线画笔

    public constructor(context: Context) : super(context) {
    }

    public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWidth = wm.defaultDisplay.width
        mHeight = mWidth
        mGridWidth = (mWidth!! - DEFAULT_MARGIN) / mGridNum
        mMargin = (mWidth!! - mGridWidth!! * mGridNum) / 2

        mGridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mGridLinePaint?.setStrokeWidth(GRID_LINE_WIDTH.toFloat())
        mGridLinePaint?.setColor(Color.parseColor("#50FF4081"))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(resolveMeasure(widthMeasureSpec, mWidth!!), resolveMeasure(heightMeasureSpec, mHeight!!))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawGridLine(canvas!!)
    }

    /**
     * 根据传入的值进行测量
     * @param measureSpec
     * @param defaultSize
     */
    fun resolveMeasure(measureSpec: Int, defaultSize: Int): Int {
        var result = 0
        val specSize = View.MeasureSpec.getSize(measureSpec)
        when (View.MeasureSpec.getMode(measureSpec)) {
            View.MeasureSpec.UNSPECIFIED -> result = defaultSize

            View.MeasureSpec.AT_MOST -> result = Math.min(specSize, defaultSize)  //设置warp_content时设置默认值

            View.MeasureSpec.EXACTLY -> {
            }

            else -> result = defaultSize
        }//设置math_parent 和设置了固定宽高值
        return result
    }

    fun drawGridLine(canvas: Canvas) {
        for (i in 0..(mGridNum + 1)) {
            val horizontalStartX = mMargin!!
            val horizontalStartY = (mGridWidth!! * i) + mMargin!!
            val horizontalStopX = mWidth!! - mMargin!!
            val horizontalStopY = horizontalStartY
            val verticalStartX = (mGridWidth!! * i) + mMargin!!
            val verticalStartY = mMargin!!
            val verticalStopX = verticalStartX
            val verticalStopY = mWidth!! - mMargin!!
            canvas.drawLine(horizontalStartX.toFloat(), horizontalStartY.toFloat(), horizontalStopX.toFloat(), horizontalStopY.toFloat(), mGridLinePaint)
            canvas.drawLine(verticalStartX.toFloat(), verticalStartY.toFloat(), verticalStopX.toFloat(), verticalStopY.toFloat(), mGridLinePaint)
        }
    }

    fun setLevel(level: Int) {
        when (level) {
            0 -> {
                mGridNum = Constants.getInstance().SIMPLE_GRID_NUM
            }
            1 -> {
                mGridNum = Constants.getInstance().NORMAL_GRID_NUM
            }
            2 -> {
                mGridNum = Constants.getInstance().HARD_GRID_NUM
            }
        }
        mGridWidth = (mWidth!! - DEFAULT_MARGIN) / mGridNum
        invalidate()
    }
}