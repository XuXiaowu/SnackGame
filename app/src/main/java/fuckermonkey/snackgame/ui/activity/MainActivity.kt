package fuckermonkey.snackgame.ui.activity

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch

import fuckermonkey.snackgame.R
import fuckermonkey.snackgame.ui.constant.Direction
import fuckermonkey.snackgame.ui.view.GameStatusListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener, GameStatusListener, CompoundButton.OnCheckedChangeListener {


    var mRunFlag = true
    var mSleepTime = 500L
    var mSaveSleepTime = 500L
    var mSpeed = 1
    var mScore = 0
    var mSelectItem = 0

    var mLevelDialog: AlertDialog.Builder? = null
    var mSettingDialog: AlertDialog.Builder? = null

    private var mIsAutoAvoid = false
    private var mIsAutoEat = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        game_view.setGameStatusListener(this)
        play_btn.setOnClickListener(this)
        faster_btn.setOnTouchListener(this)
        slowly_btn.setOnTouchListener(this)
        stop_btn.setOnClickListener(this)
        level_btn.setOnClickListener(this)
        setting_btn.setOnClickListener(this)
        left_btn.setOnClickListener(this)
        right_btn.setOnClickListener(this)
        top_btn.setOnClickListener(this)
        bottom_btn.setOnClickListener(this)
        setGameData()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.play_btn -> {
                val btn = view as Button
                when (btn.text) {
                    getString(R.string.play) -> {
                        play_btn.setText(R.string.pause)
                        stop_btn.isEnabled = true
                        GameThread().start()
                    }
                    getString(R.string.restart) -> {
                        mRunFlag = true
                        mScore = 0
                        mSpeed = 0
                        mSaveSleepTime = 500L
                        mSleepTime = 500L
                        setGameData()
                        game_view.setMoveDirection(Direction.RIGHT)
                        game_view.initData()
                        game_view.invalidate()
                        GameThread().start()
                        play_btn.setText(R.string.pause)
                        stop_btn.isEnabled = true
                        setDirectionBtnEnable(true)
                    }
                    getString(R.string.pause) -> {
                        mRunFlag = false
                        play_btn.setText(R.string.resume)
                    }
                    getString(R.string.resume) -> {
                        mRunFlag = true
                        GameThread().start()
                        play_btn.setText(R.string.pause)
                    }
                }
            }
            R.id.stop_btn -> {
                mRunFlag = false
                play_btn.setText(R.string.restart)
                stop_btn.isEnabled = false
            }
            R.id.level_btn -> {
                if (mLevelDialog == null) {
                    mLevelDialog = AlertDialog.Builder(this)
                    mLevelDialog?.setIcon(R.mipmap.ic_launcher)
                    mLevelDialog?.setTitle("Selection difficulty")
                    mLevelDialog?.setSingleChoiceItems(R.array.difficulty_array, mSelectItem, mItemClickListener)
                    mLevelDialog?.setPositiveButton(R.string.confirm, mPositiveButtonClickListener)
                    mLevelDialog?.setNegativeButton(R.string.cancel, null)
                    mLevelDialog?.show()
                } else {
                    mLevelDialog?.show()
                }
            }
            R.id.setting_btn -> {
                val view = LayoutInflater.from(this).inflate(R.layout.setting_view, null)
                val autoAvoidSwitch = view.findViewById(R.id.auto_avoid_switch) as Switch
                val autoEatSwitch = view.findViewById(R.id.auto_eat_switch) as Switch
                autoAvoidSwitch.isChecked = mIsAutoAvoid
                autoEatSwitch.isChecked = mIsAutoEat
                autoAvoidSwitch.setOnCheckedChangeListener(this)
                autoEatSwitch.setOnCheckedChangeListener(this)
                mSettingDialog = AlertDialog.Builder(this)
                mSettingDialog?.setIcon(R.mipmap.ic_launcher)
                mSettingDialog?.setTitle("Setting")
                mSettingDialog?.setView(view)
                mSettingDialog?.show()
            }
            R.id.left_btn -> {
                game_view.snackMove(Direction.LEFT)
            }
            R.id.right_btn -> {
                game_view.snackMove(Direction.RIGHT)
            }
            R.id.top_btn -> {
                game_view.snackMove(Direction.TOP)
            }
            R.id.bottom_btn -> {
                game_view.snackMove(Direction.BOTTOM)
            }
        }
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (view?.id) {
            R.id.faster_btn -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mSleepTime = (mSleepTime * 0.3).toLong()
                    }
                    MotionEvent.ACTION_UP -> {
                        mSleepTime = mSaveSleepTime
                    }
                }
            }
            R.id.slowly_btn -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mSleepTime *= 3
                    }
                    MotionEvent.ACTION_UP -> {
                        mSleepTime = mSaveSleepTime
                    }
                }
            }
        }
        return true
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        when (p0?.id) {
            R.id.auto_avoid_switch -> {
                mIsAutoAvoid = p1
                game_view.setAutoAvoid(mIsAutoAvoid)
            }
            R.id.auto_eat_switch -> {
                mIsAutoEat = p1
                game_view.setAutoEat(mIsAutoEat)
            }
        }
    }

    override fun onGameOver() {
        mRunFlag = false
        play_btn.setText(R.string.restart)
        setDirectionBtnEnable(false)
    }

    override fun onEatFood() {
        mSpeed++
        mSaveSleepTime = (mSaveSleepTime * 0.95).toLong()
        mSleepTime = mSaveSleepTime
        mScore += mSpeed
        setGameData()
    }

    fun setGameData() {
        score_view.setText("score:${mScore}")
        speed_view.setText("speed:${mSpeed}")
    }

    fun setDirectionBtnEnable(enable: Boolean) {
        left_btn.isEnabled = enable
        right_btn.isEnabled = enable
        top_btn.isEnabled = enable
        bottom_btn.isEnabled = enable
    }

    val mItemClickListener = DialogInterface.OnClickListener {
        dialogInterface, i ->
        mSelectItem = i
    }

    val mPositiveButtonClickListener = DialogInterface.OnClickListener {
        dialogInterface, i ->
        grid_view.setLevel(mSelectItem)
        game_view.setLevel(mSelectItem)
    }

    var handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            game_view.snackMove()
        }
    }

    inner class GameThread : Thread() {
        override fun run() {
            while (mRunFlag) {
                sleep(mSleepTime!!)
                handler.sendEmptyMessage(0)
            }
        }
    }
}
