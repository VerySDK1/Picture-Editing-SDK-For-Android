package com.pesdk.uisdk.widget;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pesdk.uisdk.R;
import com.pesdk.uisdk.util.Utils;
import com.vecore.base.lib.utils.CoreUtils;
import com.vecore.base.lib.utils.ThreadPoolUtils;

import java.lang.ref.SoftReference;

import androidx.annotation.StringRes;


public class SysAlertDialog {
    public interface CancelListener {
        void cancel();
    }

    public interface onDateChangedListener {
        public void onDateChange(int year, int monthOfYear, int dayOfMonth);
    }

    private static ExtProgressDialog m_dlgLoading;
    public static final int LENGTH_SHORT = 2 * 1000;
    public static final int LENGTH_LONG = 5 * 1000;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static SoftReference<Toast> wrToast = null;
    private static Object synObj = new Object();
    private static SoftReference<Toast> wrToastScore = null;
    private static Object synObjScore = new Object();

    /**
     * 创建并显示提示框
     *
     * @param context
     * @param strMessage
     */
    public static ExtProgressDialog showLoadingDialog(Context context, String strMessage) {
        return showLoadingDialog(context, strMessage, true,
                dialog -> cancelLoadingDialog());
    }

    /**
     * @param context
     * @param strId
     * @return
     */
    public static ExtProgressDialog showLoadingDialog(Context context, @StringRes int strId) {
        return showLoadingDialog(context, context.getString(strId));
    }

    private static final String TAG = "SysAlertDialog";

    /**
     * 创建并显示提示框
     *
     * @param context
     * @param strMessage
     * @param cancelable
     * @param listener
     * @return
     */
    public static ExtProgressDialog showLoadingDialog(Context context, String strMessage, boolean cancelable, DialogInterface.OnCancelListener listener) {
        if (m_dlgLoading == null) {
            m_dlgLoading = new ExtProgressDialog(context, R.style.pesdk_dialogNoDim);
            m_dlgLoading.setMessage(strMessage);
            m_dlgLoading.setIndeterminate(true);
            // m_dlgLoading.getWindow().setType(
            // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            m_dlgLoading.setCanceledOnTouchOutside(false);
            m_dlgLoading.setCancelable(cancelable);
            m_dlgLoading.setOnCancelListener(listener);
        }
        try {
            if (null != m_dlgLoading) {
                m_dlgLoading.show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return m_dlgLoading;
    }

    /**
     * 创建并显示提示框
     *
     * @param context
     * @param strId
     * @param cancelable
     * @param listener
     * @return
     */
    public static ExtProgressDialog showLoadingDialog(Context context,
                                                      int strId, boolean cancelable, DialogInterface.OnCancelListener listener) {

        return showLoadingDialog(context, context.getString(strId), cancelable,
                listener);
    }

    /**
     * 取消加载中对话框
     */
    public static synchronized void cancelLoadingDialog() {
        if (m_dlgLoading != null) {
            try {
                m_dlgLoading.cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            m_dlgLoading = null;
        }
    }


    /**
     * 创建自定义进度条对话框
     *
     * @param context
     * @return
     */
    public static ExtProgressDialog createProgressDialog(Context context) {
        return new ExtProgressDialog(context);
    }

    /**
     * 创建自定义水平进度条对话框
     *
     * @param context
     * @param message
     * @param indeterminate
     * @param cancelable
     * @param cancelListener
     * @return
     */
    public static HorizontalProgressDialog showHoriProgressDialog(Context context,
                                                                  String message, boolean indeterminate, boolean cancelable,
                                                                  DialogInterface.OnCancelListener cancelListener) {
        HorizontalProgressDialog dialog = new HorizontalProgressDialog(context);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    /**
     * 创建自定义进度条对话框
     *
     * @param context
     * @param message
     * @param indeterminate
     * @param cancelable
     * @param cancelListener
     * @return
     */
    public static ExtProgressDialog showProgressDialog(Context context,
                                                       String message, boolean indeterminate, boolean cancelable,
                                                       DialogInterface.OnCancelListener cancelListener) {
        ExtProgressDialog dialog = new ExtProgressDialog(context);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    /**
     * @param context
     * @param strId
     * @param indeterminate
     * @param cancelable
     * @param cancelListener
     * @return
     */
    public static ExtProgressDialog showProgressDialog(Context context,
                                                       int strId, boolean indeterminate, boolean cancelable,
                                                       DialogInterface.OnCancelListener cancelListener) {
        return showProgressDialog(context, context.getString(strId),
                indeterminate, cancelable, cancelListener);
    }

    public static Dialog showAlertDialog(Context context, int nMessageResId,
                                         int nPositiveBtnResId,
                                         DialogInterface.OnClickListener positiveButtonClickListener,
                                         int nNegativeBtnResId,
                                         DialogInterface.OnClickListener negativeButtonClickListener) {
        Resources res = context.getResources();
        return showAlertDialog(context, getString(res, nMessageResId),
                getString(res, nPositiveBtnResId), positiveButtonClickListener,
                getString(res, nNegativeBtnResId), negativeButtonClickListener);
    }

    public static Dialog showAlertDialog(Context context, int nTitleResId,
                                         int nMessageResId, int nPositiveBtnResId,
                                         DialogInterface.OnClickListener positiveButtonClickListener,
                                         int nNegativeBtnResId,
                                         DialogInterface.OnClickListener negativeButtonClickListener) {
        Resources res = context.getResources();
        return showAlertDialog(context, getString(res, nTitleResId),
                getString(res, nMessageResId),
                getString(res, nPositiveBtnResId), positiveButtonClickListener,
                getString(res, nNegativeBtnResId), negativeButtonClickListener);
    }

    public static Dialog showAlertDialog(Context context, String strMessage,
                                         String strPositiveBtnText,
                                         DialogInterface.OnClickListener positiveButtonClickListener,
                                         String strNegativeBtnText,
                                         DialogInterface.OnClickListener negativeButtonClickListener) {
        Dialog dlg = createAlertDialog(context, null, strMessage,
                strPositiveBtnText, positiveButtonClickListener,
                strNegativeBtnText, negativeButtonClickListener, true, null);
        dlg.show();
        return dlg;
    }

    public static Dialog showAlertDialog(Context context, String strTitle,
                                         String strMessage, String strPositiveBtnText,
                                         DialogInterface.OnClickListener positiveButtonClickListener,
                                         String strNegativeBtnText,
                                         DialogInterface.OnClickListener negativeButtonClickListener) {
        Dialog dlg = createAlertDialog(context, strTitle.equals("") ? ""
                        : strTitle, strMessage, strPositiveBtnText,
                positiveButtonClickListener, strNegativeBtnText,
                negativeButtonClickListener, true, null);
        try {
            dlg.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dlg;
    }

    public static Dialog showAlertDialog(Context context, String strTitle,
                                         String strMessage, CharSequence[] items,
                                         final DialogInterface.OnClickListener listener) {
        Dialog dlg = createAlertDialog(context,
                TextUtils.isEmpty(strTitle) ? "" : strTitle, strMessage, items,
                listener, true, null);
        try {
            dlg.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dlg;
    }

    /**
     * @param context
     * @param titleId
     * @param msgId
     * @param duration
     */
    public static void showAutoHideDialog(Context context, int titleId,
                                          int msgId, int duration) {
        showAutoHideDialog(context,
                (titleId == 0 ? "" : context.getString(titleId)),
                (msgId == 0 ? "" : context.getString(msgId)), duration);
    }

    /**
     * @param context
     * @param strTitle
     * @param strMessage
     * @param duration
     */
    public static void showAutoHideDialog(final Context context,
                                          final String strTitle, final String strMessage, final int duration) {

        ThreadPoolUtils.executeEx(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (synObj) {
                            if (wrToast != null) {
                                if (android.os.Build.VERSION.SDK_INT < 14) { // android
                                    // 4.0以上不调用cancel,cancel会造成show生成异常！
                                    if (wrToast.get() != null) {
                                        wrToast.get().cancel();
                                    }
                                }
                                if (wrToast.get() != null) {
                                    refreshToast(context, strTitle, strMessage,
                                            duration);
                                } else {
                                    newToast(context, strTitle, strMessage,
                                            duration);
                                }
                            } else {
                                newToast(context, strTitle, strMessage,
                                        duration);
                            }
                            if (wrToast.get() != null) {
                                wrToast.get().show();
                            }
                        }
                    }
                });
            }
        });

    }

    /**
     * 创建一个自定义toast
     *
     * @param context
     * @param strTitle
     * @param strMessage
     * @param duration
     */

    @SuppressLint("ShowToast")
    private static void newToast(Context context, String strTitle,
                                 String strMessage, int duration) {
        try {
            Toast toast = Toast.makeText(context, strMessage, duration);
            wrToast = new SoftReference<Toast>(toast);
            refreshToast(context, strTitle, strMessage, duration);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 刷新自定义toast
     *
     * @param context
     * @param strTitle
     * @param strMessage
     * @param duration
     */
    private static void refreshToast(Context context, String strTitle,
                                     String strMessage, int duration) {
        LayoutInflater inflate = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.pesdk_auto_hide_dialog, null);
        // v.getBackground().setAlpha(50);
        TextView textView = (TextView) v.findViewById(R.id.tvMessage);
        textView.setText(strMessage);
        textView = (TextView) v.findViewById(R.id.tvTitle);
        // LinearLayout llAutoHideDialogTilte = (LinearLayout) v
        // .findViewById(R.id.llAutoHideDialogTilte);
        if (!TextUtils.isEmpty(strTitle)) {
            textView.setText(strTitle);
        } else {
            textView.setVisibility(View.GONE);
        }
        if (null == wrToast) {
            newToast(context, strTitle, strMessage, duration);
        } else {
            wrToast.get().setView(v);
            wrToast.get().setGravity(Gravity.CENTER, 0, 0);
            wrToast.get().setDuration(duration);
        }
    }

    public static Dialog createAlertDialog(Context context, String strTitle,
                                           String strMessage, String strPositiveBtnText,
                                           DialogInterface.OnClickListener positiveButtonClickListener,
                                           String strNegativeBtnText,
                                           DialogInterface.OnClickListener negativeButtonClickListener,
                                           boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        AlertDialog ad = new AlertDialog(context);
        ad.setTitle(strTitle);
        ad.setMessage(strMessage);
        ad.setPositiveButton(strPositiveBtnText, positiveButtonClickListener);
        ad.setNegativeButton(strNegativeBtnText, negativeButtonClickListener);
        ad.setCancelable(cancelable);
        ad.setOnCancelListener(cancelListener);
        return ad;
    }

    public static Dialog createAlertDialog(Context context, String strTitle,
                                           String strMessage, CharSequence[] items,
                                           final DialogInterface.OnClickListener listener, boolean cancelable,
                                           DialogInterface.OnCancelListener cancelListener) {
        AlertDialog ad = new AlertDialog(context);
        ad.setTitle(strTitle);
        ad.setMessage(strMessage);
        ad.setItems(items, listener);
        ad.setCancelable(cancelable);
        ad.setOnCancelListener(cancelListener);
        return ad;
    }

    //

    /**
     * 显示列表对框菜单
     *
     * @param context
     * @param strTitle
     * @param arrItems
     * @param itemClickListener
     * @return
     */
    public static Dialog showListviewAlertMenu(Context context,
                                               String strTitle, CharSequence[] arrItems,
                                               DialogInterface.OnClickListener itemClickListener) {
        AlertListViewDialog ad = new AlertListViewDialog(context, strTitle,
                arrItems, itemClickListener);
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(true);
        ad.show();

        return ad;
    }

    public static Dialog showListviewAlertMenu(Context context,
                                               String strTitle, CharSequence[] arrItems,
                                               DialogInterface.OnClickListener itemClickListener,
                                               CancelListener mcancel) {
        AlertListViewDialog ad = new AlertListViewDialog(context, strTitle,
                arrItems, itemClickListener);
        ad.setcanclelistener(mcancel);
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(true);
        ad.show();

        return ad;
    }

    public static Dialog showSelectDialog(Context context, String strTitle,
                                          CharSequence[] arrItems,
                                          DialogInterface.OnClickListener itemClickListener) {
        AlertListViewDialog ad = new AlertListViewDialog(context, strTitle,
                arrItems, itemClickListener, true);
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.show();
        return ad;
    }

    private static String getString(Resources res, int nStringResId) {
        try {
            return res.getString(nStringResId);
        } catch (Resources.NotFoundException ex) {

        }
        return null;
    }
}

class AlertDialog extends Dialog {

    private TextView m_tvMessage;
    private String m_strMessage;
    private String m_strTitle;
    private DialogInterface.OnClickListener m_positiveButtonClickListener;
    private DialogInterface.OnClickListener m_negativeButtonClickListener;
    private String m_strPositiveButtonText;
    private String m_strNegativeButtonText;
    private boolean m_bCreated = false;
    private View m_vContentView;
    private CharSequence[] m_arrItems;
    private ListView m_lvItems;
    private OnClickListener m_listenerItems;

    public AlertDialog(Context context) {
        super(context, R.style.pesdk_dialog);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        m_vContentView = inflater.inflate(R.layout.pesdk_alert_dialog, null);
        setContentView(m_vContentView);
        m_tvMessage = (TextView) m_vContentView.findViewById(R.id.tvMessage);
        Button btnTmp = (Button) m_vContentView.findViewById(R.id.btnNegative);
        doSetNegativeButton(m_strNegativeButtonText);
        btnTmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(DialogInterface.BUTTON_NEGATIVE);
            }
        });
        btnTmp = (Button) m_vContentView.findViewById(R.id.btnPositive);
        doSetPositiveButton(m_strPositiveButtonText);
        btnTmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(DialogInterface.BUTTON_POSITIVE);
            }
        });
        setTitle(m_strTitle);
        setMessage(m_strMessage);
        m_lvItems = (ListView) m_vContentView.findViewById(R.id.lvItems);
        m_lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (m_listenerItems != null) {
                    m_listenerItems.onClick(AlertDialog.this, position);
                }
                dismiss();
            }
        });
        if (null != m_arrItems && m_listenerItems != null) {
            setItems(m_arrItems, m_listenerItems);
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        this.onWindowAttributesChanged(lp);
        setButtonBackground();
        m_bCreated = true;

    }

    public View getContentView() {
        return m_vContentView;
    }

    /**
     * @param items
     * @param listener
     */
    public void setItems(CharSequence[] items, OnClickListener listener) {
        m_arrItems = items;
        m_listenerItems = listener;
        if (null != m_lvItems) {
            m_lvItems.setAdapter(new ArrayAdapter<CharSequence>(getContext(),
                    R.layout.pesdk_alert_listview_dialog_item, R.id.tvDialogItem,
                    items));
            m_lvItems.setVisibility(View.VISIBLE);
            m_vContentView.findViewById(R.id.llButtons)
                    .setVisibility(View.GONE);
        }
    }

    public void setMessage(String strMessage) {
        m_strMessage = strMessage;
        if (null != m_tvMessage) {
            if (!TextUtils.isEmpty(strMessage)) {
                m_tvMessage.setText(strMessage);
                m_tvMessage.setVisibility(View.VISIBLE);
            } else {
                m_tvMessage.setVisibility(View.GONE);
            }
        }
    }

    public void setPositiveButton(String strText,
                                  DialogInterface.OnClickListener clickListener) {
        m_positiveButtonClickListener = clickListener;
        m_strPositiveButtonText = strText;
        if (m_bCreated) {
            doSetPositiveButton(strText);
        }
    }

    private void doSetPositiveButton(String strText) {
        Button btnPositive = (Button) this.findViewById(R.id.btnPositive);
        if (TextUtils.isEmpty(strText)) {
            btnPositive.setVisibility(View.GONE);
        } else {
            btnPositive.setText(strText);
            btnPositive.setVisibility(View.VISIBLE);
        }
    }

    public void setNegativeButton(String strText,
                                  DialogInterface.OnClickListener clickListener) {
        m_negativeButtonClickListener = clickListener;
        m_strNegativeButtonText = strText;
        if (m_bCreated) {
            doSetNegativeButton(strText);
        }
    }

    private void doSetNegativeButton(String strText) {
        Button btnNegative = (Button) this.findViewById(R.id.btnNegative);
        if (TextUtils.isEmpty(strText)) {
            btnNegative.setVisibility(View.GONE);
        } else {
            btnNegative.setText(strText);
            btnNegative.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonBackground() {
        Button btnPositive = (Button) this.findViewById(R.id.btnPositive);
        Button btnNegative = (Button) this.findViewById(R.id.btnNegative);
        if (btnPositive.getVisibility() == View.GONE) {
            // btnNegative.setBackgroundResource(R.drawable.alert_dialog_normal_button);
            ImageView iv = (ImageView) this.findViewById(R.id.ivInterval);
            iv.setVisibility(View.GONE);
        } else if (btnNegative.getVisibility() == View.GONE) {
            // btnPositive.setBackgroundResource(R.drawable.alert_dialog_normal_button);
            ImageView iv = (ImageView) this.findViewById(R.id.ivInterval);
            iv.setVisibility(View.GONE);
        }
    }

    protected void onButtonClick(int whichButton) {
        if (whichButton == BUTTON_POSITIVE) {
            if (m_positiveButtonClickListener != null) {
                m_positiveButtonClickListener.onClick(this, whichButton);
            }
            this.cancel();
        } else if (whichButton == BUTTON_NEGATIVE) {
            if (m_negativeButtonClickListener != null) {
                m_negativeButtonClickListener.onClick(this, whichButton);
            }
            this.cancel();
        }
    }
}

class AlertListViewDialog extends Dialog {

    private SysAlertDialog.CancelListener ml;

    public void setcanclelistener(SysAlertDialog.CancelListener _ml) {
        ml = _ml;

    }

    private String m_strTitle;
    private CharSequence[] m_arrItems;
    private DialogInterface.OnClickListener m_listenerItemClick;
    private boolean m_bSelect;

    public AlertListViewDialog(Context context, String strTitle,
                               CharSequence[] arrItems, DialogInterface.OnClickListener itemClick) {
        this(context, strTitle, arrItems, itemClick, false);
    }

    public AlertListViewDialog(Context context, String strTitle,
                               CharSequence[] arrItems, DialogInterface.OnClickListener itemClick,
                               boolean select) {
        super(context, select ? R.style.pesdk_selectDialog : R.style.pesdk_listviewDialog);
        setCanceledOnTouchOutside(true);
        m_listenerItemClick = itemClick;
        m_arrItems = arrItems;
        m_strTitle = strTitle;
        m_bSelect = select;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.pesdk_alert_listview_dialog, null);
        view.setMinimumWidth(10000);
        TextView tvTitle = Utils.$(view, R.id.tvTitle);
        if (!TextUtils.isEmpty(m_strTitle)) {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(m_strTitle);
        }
        ListView lvContent = Utils.$(view, R.id.lvContent);
        lvContent.setAdapter(new ArrayAdapter<CharSequence>(getContext(),
                R.layout.pesdk_alert_listview_dialog_item, m_arrItems));
        if (null != m_listenerItemClick) {
            lvContent
                    .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent,
                                                View view, int position, long id) {
                            AlertListViewDialog.this.cancel();
                            m_listenerItemClick.onClick(
                                    AlertListViewDialog.this, position);
                        }
                    });
        }
        lvContent.setFooterDividersEnabled(false);
        LinearLayout.LayoutParams listLp = (LinearLayout.LayoutParams) lvContent.getLayoutParams();
        int dividerHeight = CoreUtils.dip2px(getContext(), 1);
        listLp.height = (getContext().getResources().getDimensionPixelSize(R.dimen.dp_45) + dividerHeight) * lvContent.getCount() - dividerHeight;
        Button btnAlertCancel = Utils.$(view, R.id.btnAlertCancel);
        if (m_bSelect) {
            btnAlertCancel.setVisibility(View.GONE);
        }
        btnAlertCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertListViewDialog.this.cancel();
                if (null != ml) {
                    ml.cancel();
                }
            }
        });
        if (m_bSelect) {
            Utils.$(view, R.id.tvTitle).setPadding(10, 10, 10, 10);
        }
        setContentView(view);
        {
            //放在show()之后，不然有些属性是没有效果的，比如height和width
            Window dialogWindow = getWindow();
            DisplayMetrics display = CoreUtils.getMetrics();
            WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            // 设置宽度
            p.width = (int) (display.widthPixels * 0.9); // 宽度设置为屏幕的0.9
            p.gravity = Gravity.CENTER_VERTICAL;
            p.gravity = m_bSelect ? Gravity.CENTER_VERTICAL : Gravity.BOTTOM;
            //p.alpha = 0.8f;//设置透明度
            dialogWindow.setAttributes(p);
        }
    }

}
