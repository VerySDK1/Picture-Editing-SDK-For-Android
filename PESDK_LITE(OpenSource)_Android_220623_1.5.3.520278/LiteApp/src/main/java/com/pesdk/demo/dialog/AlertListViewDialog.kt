package com.pesdk.demo.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import com.pesdk.demo.R
import com.vecore.base.lib.utils.CoreUtils

/**
 * 列表弹窗
 */
class AlertListViewDialog(
        context: Context,
        var arrItems: Array<String>,
        var listenerItemClick: DialogInterface.OnClickListener,
        var select: Boolean = false
) : Dialog(context, if (select) R.style.pesdk_selectDialog else R.style.pesdk_listviewDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams")
        val view: View = inflater.inflate(R.layout.dialog_listview, null)
        view.minimumWidth = 10000

        val lvContent = view.findViewById<View>(R.id.lvContent) as ListView
        lvContent.adapter = ArrayAdapter(context, R.layout.dialog_listview_item, arrItems)
        lvContent.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            dismiss()
            listenerItemClick.onClick(this@AlertListViewDialog, position)
        }
        lvContent.setFooterDividersEnabled(false)

        val listLp = lvContent.layoutParams as LinearLayout.LayoutParams
        val dividerHeight = CoreUtils.dip2px(context, 1f)
        listLp.height = (context.resources.getDimensionPixelSize(R.dimen.dp_45) + dividerHeight) * lvContent.count - dividerHeight
        setContentView(view)
        //放在show()之后，不然有些属性是没有效果的，比如height和width
        window?.let {
            // 获取对话框当前的参数值
            val p = it.attributes
            // // 宽度设置为屏幕的0.9
            p.width = (CoreUtils.getMetrics().widthPixels * 0.9).toInt()
            p.gravity = if (select) Gravity.CENTER_VERTICAL else Gravity.BOTTOM
            //p.alpha = 0.8f;//设置透明度
            it.attributes = p
        }
    }
}