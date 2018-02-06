package cn.tqp.exoplayer.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.tqp.exoplayer.R;

/**
 * Created by TQP on 2016/9/13.
 */
public class ToastUtils {

    private static Toast toast;

    /**
     * 显示提示信息
     * @param context
     * @param msg            文本内容
     */
    public static void showToast(Context context, String msg) {
        showToast(context, msg, 0, R.drawable.toast_bg);
    }

    /**
     * 显示提示信息
     * @param context
     * @param msgId            文本内容
     */
    public static void showToast(Context context, int msgId) {
        if (context == null || msgId <= 0) return;
        showToast(context, msgId, 0, R.drawable.toast_bg);
    }

    /**
     * 显示提示信息
     * @param context
     * @param resId          文本资源Id
     * @param imageRes       提示图标资源id
     */
    public static void showToast(Context context, int resId, int imageRes) {
        if (context == null || resId <= 0) return;
        showToast(context, context.getString(resId), imageRes, R.drawable.toast_bg);
    }

    /**
     * 显示提示信息
     * @param context
     * @param msg            文本内容
     * @param imageRes       提示图标资源id
     */
    public static void showToast(Context context, String msg, int imageRes) {
        showToast(context, msg, imageRes, R.drawable.toast_bg);
    }

    /**
     * 显示提示信息
     * @param context
     * @param resId          文本资源Id
     * @param imageRes       提示图标资源id
     * @param backgroundRes  提示框背景图
     */
    public static void showToast(Context context, int resId, int imageRes, int backgroundRes) {
        if (context == null || resId <= 0) return;
        showToast(context, context.getString(resId), imageRes, backgroundRes);
    }

    /**
     * 显示提示信息
     * @param context
     * @param msg            文本内容
     * @param imageRes       提示图标资源id
     * @param backgroundRes  提示框背景图
     */
    public static void showToast(Context context, String msg, int imageRes, int backgroundRes) {
        if (context == null || TextUtils.isEmpty(msg)) return;
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM	, 0, 100);

        LinearLayout toastView = (LinearLayout) toast.getView();
        toastView.setOrientation(LinearLayout.HORIZONTAL);
        TextView messageTextView = (TextView) toastView.getChildAt(0);
        messageTextView.setTextSize(25);
        if (backgroundRes > 0) {
            toastView.setBackgroundResource(backgroundRes);
        }
        if (imageRes > 0) {
            ImageView iv = new ImageView(context);
            iv.setImageResource(imageRes);
            iv.setPadding(0,0,5,0);
            toastView.addView(iv, 0);
        }

        // 修改布局的大小
        float density = context.getResources().getDisplayMetrics().density;
        int padding = (int)(density * 10);
        toastView.setGravity(Gravity.CENTER);
//        toastView.setMinimumWidth((int)(density * 170));
        toastView.setPadding(padding, padding, padding, padding);
        toast.show();
    }

}
