package cn.tqp.exoplayer.exoplayerui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.IntDef;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

/**
 * Created by tangqipeng on 2018/1/25.
 */

public class ExoPlayerUtils {

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    //获取屏幕原始尺寸高度，包括虚拟功能键高度
    public static int getDpi(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
//            dpi=displayMetrics.heightPixels;
            dpi = displayMetrics.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    /**
     * Get activity from context object
     *
     * @param context something
     * @return object of Activity or null if it is not Activity
     */
    public static Activity scanForActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * Get AppCompatActivity from context
     *
     * @param context
     * @return AppCompatActivity if it's not null
     */
    private static AppCompatActivity getAppCompActivity(Context context) {
        if (context == null) return null;
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getAppCompActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    public static void showActionBar(Context context) {
        ActionBar ab = getAppCompActivity(context).getSupportActionBar();
        if (ab != null) {
//            ab.setShowHideAnimationEnabled(false);
            ab.show();
        }
        scanForActivity(context)
                .getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void showActionBarAndBottomUiMenu(Context context) {
        ActionBar ab = getAppCompActivity(context).getSupportActionBar();
        if (ab != null) {
//            ab.setShowHideAnimationEnabled(false);
            ab.hide();
        }

        scanForActivity(context)
                .getWindow()
                .clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void hideActionBarAndBottomUiMenu(Context context) {
        ActionBar ab = getAppCompActivity(context).getSupportActionBar();
        if (ab != null) {
//            ab.setShowHideAnimationEnabled(false);
            ab.hide();
        }

        scanForActivity(context)
                .getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


}
