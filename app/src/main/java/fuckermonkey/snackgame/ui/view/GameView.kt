package fuckermonkey.snackgame.ui.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import fuckermonkey.snackgame.ui.constant.Constants
import fuckermonkey.snackgame.ui.constant.Direction
import java.util.*

/**
 * Created by xuxiaowu on 2017/6/12.
 */
class GameView : View {

    val GRID_LINE_WIDTH = 1
    val DEFAULT_MARGIN = 20

    private var mWidth: Int? = null
    private var mHeight: Int? = null
    private var mGridWidth: Int? = null
    private var mMargin: Int? = null
    private var mGridNum = Constants.getInstance().SIMPLE_GRID_NUM

    private var mIsAutoAvoid = false
    private var mIsAutoEat = false

    private var mSnackPaint: Paint? = null //Snack的画笔
    private var mFoodPaint: Paint? = null //食物的画笔

    private var mMoveDirection = Direction.RIGHT

    private var mGameStatusListener: GameStatusListener? = null
    private val mSnack: LinkedList<Point> = LinkedList()
    private var mFood: Point = Point()
    private val mRandom = Random()

    public constructor(context: Context) : super(context) {
        initData()
    }

    public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initData()
    }

    public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initData()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(resolveMeasure(widthMeasureSpec, mWidth!!), resolveMeasure(heightMeasureSpec, mHeight!!))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawSnack(canvas!!)
        drawFood(canvas!!)
    }

    fun setGameStatusListener(listener: GameStatusListener) {
        mGameStatusListener = listener
    }

    fun setLevel(level: Int) {
        when (level) {
            0 -> {
                mGridNum = Constants.getInstance().SIMPLE_GRID_NUM
            }
            1 -> {
                mGridNum = Constants.getInstance().SIMPLE_GRID_NUM
            }
            2 -> {
                mGridNum = Constants.getInstance().SIMPLE_GRID_NUM
            }
        }
        mGridWidth = (mWidth!! - DEFAULT_MARGIN) / mGridNum
        initFood()
        invalidate()
    }

    init {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWidth = wm.defaultDisplay.width
        mHeight = mWidth
        mGridWidth = (mWidth!! - DEFAULT_MARGIN) / mGridNum
        mMargin = (mWidth!! - mGridWidth!! * mGridNum) / 2

        mSnackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mSnackPaint?.setStrokeWidth(GRID_LINE_WIDTH.toFloat())
        mSnackPaint?.setColor(Color.parseColor("#4CAF50"))

        mFoodPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mFoodPaint?.setStrokeWidth(GRID_LINE_WIDTH.toFloat())
        mFoodPaint?.setColor(Color.parseColor("#E91E63"))

        mGridNum = Constants.getInstance().SIMPLE_GRID_NUM
    }

    fun initData() {
        initSnack()
        initFood()
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

            View.MeasureSpec.AT_MOST -> result = Math.min(specSize, defaultSize) //设置warp_content时设置默认值

            View.MeasureSpec.EXACTLY -> {
            }

            else -> result = defaultSize
        }//设置math_parent 和设置了固定宽高值
        return result
    }

    fun drawSnack(canvas: Canvas) {
        for (it in mSnack.iterator()) {
            val snackBlock = Rect()
            snackBlock.left = mGridWidth!! * it.x + mMargin!!
            snackBlock.right = snackBlock.left + mGridWidth!!
            snackBlock.top = mGridWidth!! * it.y + mMargin!!
            snackBlock.bottom = snackBlock.top + mGridWidth!!
            canvas.drawRect(snackBlock, mSnackPaint)
        }
    }

    fun drawFood(canvas: Canvas) {
        val foodBlock = Rect()
        foodBlock.left = mGridWidth!! * mFood.x + mMargin!!
        foodBlock.right = foodBlock.left + mGridWidth!!
        foodBlock.top = mGridWidth!! * mFood.y + mMargin!!
        foodBlock.bottom = foodBlock.top + mGridWidth!!
        canvas.drawRect(foodBlock, mFoodPaint)
    }

    fun initSnack() {
        mSnack.clear()
        for (i in 0..4) {
            val point = Point(i, 0)
            mSnack.add(point)
        }
    }

    fun initFood() {
        val x = mRandom.nextInt(mGridNum)
        val y = mRandom.nextInt(mGridNum)
        mFood.set(x, y)
    }

    /**
     *
     */
    fun snackMove(moveDirection: Direction) {
        if (!checkMoveDirection(moveDirection)) return
        mMoveDirection = moveDirection

        val food = checkNextPointIsFood()
        if (food != null) {
            eatFood(food)
            invalidate()
        } else {
            snackMove()
        }

    }

    fun snackMove() {
        val food = checkNextPointIsFood()
        if (food != null) {
            eatFood(food)
        } else {
            updateSnack()
            checkBoundary()
        }
        invalidate()
        if (mIsAutoAvoid) avoidBoundary()
        if (mIsAutoEat) turnToFood()
    }

    fun updateSnack() {
        val head = getNextPoint()
        mSnack.removeFirst() //删除尾部
        mSnack.add(head) //添加头

    }

    /**
     * 检查是否到达边界
     */
    fun checkBoundary() {
        when (mMoveDirection) {
            Direction.LEFT -> {
                if (mSnack.last.x < 0) {
                    mGameStatusListener?.onGameOver()
                }
            }
            Direction.RIGHT -> {
                if (mSnack.last.x == mGridNum) {
                    mGameStatusListener?.onGameOver()
                }
            }
            Direction.TOP -> {
                if (mSnack.last.y < 0) {
                    mGameStatusListener?.onGameOver()
                }
            }
            Direction.BOTTOM -> {
                if (mSnack.last.y == mGridNum) {
                    mGameStatusListener?.onGameOver()
                }
            }
        }
    }

    /**
     * 检查运动方向是否有冲突
     */
    fun checkMoveDirection(moveDirection: Direction): Boolean {
        var result = true
        if (moveDirection == mMoveDirection) result = false
        else {
            when (mMoveDirection) {
                Direction.LEFT -> {
                    if (moveDirection == Direction.RIGHT) result = false
                }
                Direction.RIGHT -> {
                    if (moveDirection == Direction.LEFT) result = false
                }
                Direction.TOP -> {
                    if (moveDirection == Direction.BOTTOM) result = false
                }
                Direction.BOTTOM -> {
                    if (moveDirection == Direction.TOP) result = false
                }
            }
        }
        return result
    }

    /**
     * 规避边界
     */
    fun avoidBoundary() {
        val nextPoint = Point(mSnack.last)
        when (mMoveDirection) {
            Direction.LEFT -> {
                if (nextPoint.x == 0) {
                    if (nextPoint.y == 0) {
                        mMoveDirection = Direction.BOTTOM
                    } else {
                        mMoveDirection = Direction.TOP
                    }
                }
            }
            Direction.RIGHT -> {
                if (nextPoint.x == mGridNum - 1) {
                    if (nextPoint.y == 0) {
                        mMoveDirection = Direction.BOTTOM
                    } else {
                        mMoveDirection = Direction.TOP
                    }
                }
            }
            Direction.TOP -> {
                if (nextPoint.y == 0) {
                    if (nextPoint.x == 0) {
                        mMoveDirection = Direction.RIGHT
                    } else {
                        mMoveDirection = Direction.LEFT
                    }
                }
            }
            Direction.BOTTOM -> {
                if (nextPoint.y == mGridNum - 1) {
                    if (nextPoint.x == 0) {
                        mMoveDirection = Direction.RIGHT
                        Log.e("TAG", "auto turn right")
                    } else {
                        mMoveDirection = Direction.LEFT
                        Log.e("TAG", "auto turn left")
                    }
                }
            }
        }
    }

    /**
     * 转到食物的方向
     */
    fun turnToFood() {
        val head = Point(mSnack.last)
        if (mMoveDirection == Direction.LEFT || mMoveDirection == Direction.RIGHT) {
            if (head.x == mFood.x) {
                if (head.y > mFood.y) {
                    mMoveDirection = Direction.TOP
                } else {
                    mMoveDirection = Direction.BOTTOM
                }
            }
        } else {
            if (head.y == mFood.y) {
                if (head.x > mFood.x) {
                    mMoveDirection = Direction.LEFT
                } else {
                    mMoveDirection = Direction.RIGHT
                }
            }
        }
    }

    /**
     * 检查下一个点是否有食物
     */
    fun checkNextPointIsFood(): Point? {
        val nextPoint = getNextPoint()
        if (nextPoint.x == mFood.x && nextPoint.y == mFood.y) {
            return nextPoint
        } else {
            return null
        }
    }

    /**
     * 获取下一个点
     */
    fun getNextPoint(): Point {
        val nextPoint = Point(mSnack.last)
        when (mMoveDirection) {
            Direction.LEFT -> {
                nextPoint.x -= 1
            }
            Direction.RIGHT -> {
                nextPoint.x += 1
            }
            Direction.TOP -> {
                nextPoint.y -= 1
            }
            Direction.BOTTOM -> {
                nextPoint.y += 1
            }
        }
        return nextPoint
    }

    fun eatFood(food: Point) {
        mSnack.add(food)
        initFood()
        mGameStatusListener?.onEatFood()
    }

    fun setMoveDirection(direction: Direction) {
        mMoveDirection = direction
    }

    fun getMoveDirection(): Direction {
        return mMoveDirection
    }

    fun setAutoAvoid(b: Boolean){
        mIsAutoAvoid = b
    }

    fun setAutoEat(b: Boolean){
        mIsAutoEat = b
    }

}
