package cn.tqp.exoplayer.exoplayerui;

import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Created by tangqipeng on 2018/1/26.
 */

public class ExoPlayerListener {


    public interface SwitchoverWindow{

        void changeWindowIndex(int windowIndex);

    }

    public interface PlayerControlListener{

        void singleTouch(MotionEvent ev);

        void doubleTouch(MotionEvent e);

        void notifySoundVisible(boolean isShow);

        void notifySoundChanged(float curr);

        void notifyLightingVisible(boolean isShow);

        void notifyLightingSetting(float curr);

        void notifyPanelSeekStart();

        void notifyPanelSeekChange(PointF point1, PointF point2);

        void notifyPanelSeekEnd();

    }

}
