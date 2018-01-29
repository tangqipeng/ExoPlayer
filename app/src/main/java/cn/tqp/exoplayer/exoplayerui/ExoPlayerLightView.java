package cn.tqp.exoplayer.exoplayerui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.tqp.exoplayer.R;

/**
 * Created by tangqipeng on 2018/1/29.
 */

public class ExoPlayerLightView extends LinearLayout {

    private Context mContext;
    private TextView txtlightPercentage;

    public ExoPlayerLightView(Context context) {
        this(context, null);
    }

    public ExoPlayerLightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.exoplayer_light_view, this);
        txtlightPercentage = (TextView) findViewById(R.id.txt_light_percentage);
    }

    public void setLightPercentage(float percentage){
        if (percentage >= 100){
            percentage = 99;
        }
        txtlightPercentage.setText(String.format("%.0f%%", percentage * 100));
    }


}
