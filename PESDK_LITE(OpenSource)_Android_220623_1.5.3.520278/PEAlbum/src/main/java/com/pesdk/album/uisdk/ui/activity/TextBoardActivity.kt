package com.pesdk.album.uisdk.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.pesdk.album.R
import com.vecore.base.lib.utils.CoreUtils
import com.vecore.base.lib.utils.InputUtls
import com.pesdk.album.uisdk.listener.OnTtfListener
import com.pesdk.album.uisdk.ui.contract.TextBoardContracts.Companion.INTENT_TEXT_BOARD
import com.pesdk.album.uisdk.ui.fragment.TtfFragment
import com.pesdk.album.uisdk.utils.AlbumPathUtils
import com.pesdk.album.uisdk.utils.AlbumUtils
import com.vesdk.common.base.BaseActivity
import com.vesdk.common.base.BaseFragment
import com.vesdk.common.ui.dialog.OptionsDialog
import com.vesdk.common.listener.PopupDialogListener
import kotlinx.android.synthetic.main.album_activity_preview.btnClose
import kotlinx.android.synthetic.main.album_activity_text_board.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.abs

/**
 * 文字图片
 */
class TextBoardActivity : BaseActivity(), OnTtfListener {

    companion object {

        @JvmStatic
        fun newInstance(context: Context): Intent {
            return Intent(context, TextBoardActivity::class.java)
        }

    }

    /**
     * 根布局
     */
    private var mRoot: View? = null

    /**
     * 键盘
     */
    private var mUsableHeightPrevious = 0
    private val mFrameRect = Rect()
    private var mDisplayMetrics: DisplayMetrics? = null

    /**
     * fragment
     */
    private var mCurrentFragment: BaseFragment? = null

    /**
     * 字体
     */
    private lateinit var mTtfFragment: TtfFragment

    override fun init() {
        initView()

        //输入框
        mDisplayMetrics = CoreUtils.getMetrics()
        mRoot = window.decorView.findViewById(android.R.id.content)
        mRoot?.viewTreeObserver?.addOnGlobalLayoutListener { possiblyResizeChildOfContent() }
    }

    private fun initView() {
        btnClose.setOnClickListener { onBackPressed() }
        btnSure.setOnClickListener { onSure() }

        //输入框
        etEditPic.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                val text = s.toString().trim { it <= ' ' }
                val arr = text.split("\n".toRegex()).toTypedArray()
                var target = ""
                var temp: String
                val list = ArrayList<String>()
                for (i in arr.indices) {
                    temp = arr[i]
                    if (i < arr.size - 1) {
                        list.add(temp)
                    }
                    if (target.length < temp.length) {
                        target = temp
                    }
                }
                etEditPic.add(list)
                // 计算字体大小
                val textSize: Int = calculatedSize(
                    target, Paint(), etEditPic.width,
                    etEditPic.height / 6
                )
                etEditPic.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            }
        })
        etEditPic.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_ENTER && etEditPic.lineCount >= 6 }

        //颜色
        txColorPicker.setColorListener { color, _ ->
            etEditPic.textColor = color
        }

        //字体
        mTtfFragment = TtfFragment.newInstance()

        //背景
        bgColorPicker.setColorListener { color, _ ->
            etEditPic.bgColor = color
        }

        //对齐


        //菜单
        btnKeyboard.setOnClickListener {
            showInput()
        }
        btnColor.setOnClickListener {
            hideInput()
            txColorPicker.visibility = VISIBLE
            flFragment.visibility = GONE
            bgColorPicker.visibility = GONE
        }
        btnTtf.setOnClickListener {
            hideInput()
            txColorPicker.visibility = GONE
            flFragment.visibility = VISIBLE
            bgColorPicker.visibility = GONE
            changeFragment(mTtfFragment)
        }
        btnBackground.setOnClickListener {
            hideInput()
            txColorPicker.visibility = GONE
            flFragment.visibility = GONE
            bgColorPicker.visibility = VISIBLE
        }
        btnAlign.setOnClickListener {
            hideInput()
            txColorPicker.visibility = GONE
            flFragment.visibility = GONE
            bgColorPicker.visibility = GONE
        }

    }


    /**
     * 切换fragment
     */
    private fun changeFragment(fragment: BaseFragment) {
        if (mCurrentFragment == fragment) {
            return
        }
        mCurrentFragment?.let {
            supportFragmentManager.beginTransaction().hide(it).commitAllowingStateLoss()
        }
        if (!fragment.isAdded) {
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            supportFragmentManager.beginTransaction()
                .add(R.id.flFragment, fragment)
                .show(fragment)
                .commitAllowingStateLoss()
        } else {
            supportFragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss()
        }
        mCurrentFragment = fragment
    }

    /**
     * 键盘
     */
    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != mUsableHeightPrevious) {
            val usableHeightSansKeyboard = mRoot!!.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // 键盘弹出
                mDisplayMetrics?.let {
                    //获取屏幕的高度
                    val screenHeight = mRoot?.rootView?.height ?: 0
                    flMenu.getLocalVisibleRect(mFrameRect)
                    val lp =
                        LinearLayout.LayoutParams(it.widthPixels, screenHeight - usableHeightNow)
                    flMenu.layoutParams = lp

                    val lp1 =
                        FrameLayout.LayoutParams(it.widthPixels, screenHeight - usableHeightNow)
                    txColorPicker.layoutParams = lp1
                    bgColorPicker.layoutParams = lp1
                }

            }
            mRoot?.requestLayout()
            mUsableHeightPrevious = usableHeightNow
        }
    }

    /**
     * 显示高度
     */
    private fun computeUsableHeight(): Int {
        val r = Rect()
        mRoot?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top
    }

    /**
     * 字体
     */
    override fun onAddTtf(path: String) {
        etEditPic.ttf = path
        if (path == "") {
            etEditPic.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        } else {
            etEditPic.typeface = Typeface.createFromFile(path)
        }
    }


    /**
     * 计算字体大小
     */
    private fun calculatedSize(text: String, p: Paint, width: Int, height: Int): Int {
        var textSize = 100
        var fm: Paint.FontMetrics
        p.isAntiAlias = true
        p.textSize = textSize.toFloat()
        while (true) {
            p.textSize = textSize.toFloat()
            if (p.measureText(text) > width) {
                textSize -= 2
            } else {
                fm = p.fontMetrics
                textSize -= if (abs(fm.leading) + abs(fm.ascent) + abs(fm.descent) > height
                ) {
                    2
                } else {
                    break
                }
            }
        }
        return textSize
    }

    /**
     * 显示键盘
     */
    private fun showInput() {
        InputUtls.showInput(etEditPic)
    }

    /**
     * 隐藏键盘
     */
    private fun hideInput() {
        InputUtls.hideKeyboard(etEditPic)
    }


    /**
     * 返回弹窗
     */
    private fun onQuitAlert() {
        OptionsDialog
            .create(this, "退出", "确定放弃当前操作?",
                cancelable = true, cancelTouch = true, listener = object : PopupDialogListener {

                    override fun onDialogSure() {
                        finish()
                    }

                    override fun onDialogCancel() {

                    }

                })
            .show()
    }

    override fun getLayoutId(): Int {
        return R.layout.album_activity_text_board
    }

    override fun onBackPressed() {
        if (etEditPic.text.toString() != "") {
            onQuitAlert()
        } else {
            finish()
        }
    }

    private fun onSure() {
        hideInput()
        etEditPic.isFocusable = false
        val path = AlbumPathUtils.getImagePath("text_${AlbumUtils.randomId}.png")
        //确定
        etEditPic.isDrawingCacheEnabled = true
        val mBitmap: Bitmap = etEditPic.drawingCache
        val file = File(path)
        if (file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            val out = FileOutputStream(file)
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        etEditPic.isDrawingCacheEnabled = false
        mBitmap.recycle()

        val intent = Intent().run {
            putExtra(INTENT_TEXT_BOARD, path)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        hideInput()
    }


}