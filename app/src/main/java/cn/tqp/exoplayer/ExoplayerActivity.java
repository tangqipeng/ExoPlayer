package cn.tqp.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import cn.tqp.exoplayer.exoplayerui.ExoPlayerView;

/**
 * Created by tangqipeng on 2018/1/24.
 */

public class ExoplayerActivity extends AppCompatActivity {

    private ConstraintLayout constraintLayout;
    private ExoPlayerView exoPlayerView;
    private SimpleExoPlayer simpleExoPlayer;
    private DefaultTrackSelector trackSelector;
//    private EventLogger eventLogger;

    private int playerWidth, playerHeight;

    private DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);
        getSupportActionBar().hide();

        //常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        constraintLayout = (ConstraintLayout) findViewById(R.id.constrainlayout);

        /**
         * Create Simple Exoplayer Player
         */
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
//        eventLogger = new EventLogger(trackSelector);

        exoPlayerView = (ExoPlayerView) findViewById(R.id.exoplayerview);
//        exoPlayerView.setControllerVisibilityListener(this);
        exoPlayerView.requestFocus();

        playerWidth = getScreenWidth(this);

        playerHeight = playerWidth * 9 / 16;

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, playerHeight);

        exoPlayerView.setLayoutParams(layoutParams);

//        simpleExoPlayerView.getOverlayFrameLayout().addView();

//        exoPlayerView.setExoPlayerViewContainer(constraintLayout);
        exoPlayerView.setExoPlayerViewContainer((ViewGroup) exoPlayerView.getParent());

        exoPlayerView.setPlayer(simpleExoPlayer);

        MediaSource secondSource1 = buildMediaSource(Uri.parse("http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8"), null, true, null, null);

        MediaSource secondSource2 = buildMediaSource(Uri.parse(getResources().getString(R.string.url_hls1)), null, true, null, null);

        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(secondSource1, secondSource2);

        simpleExoPlayer.prepare(concatenatedSource);

//        simpleExoPlayer.setShuffleModeEnabled(true);

        simpleExoPlayer.setPlayWhenReady(true);

//        simpleExoPlayer.addListener(eventLogger);
    }


    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((DemoApplication) getApplication()).buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension, boolean useBandwidthMeter, @Nullable Handler handler, @Nullable MediaSourceEventListener listener) {
        @C.ContentType
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri) : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)), buildDataSourceFactory(false)).createMediaSource(uri, handler, listener);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)), buildDataSourceFactory(false)).createMediaSource(uri, handler, listener);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter)).createMediaSource(uri, handler, listener);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter)).createMediaSource(uri, handler, listener);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onStop() {
        super.onStop();
        simpleExoPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleExoPlayer.release();
        simpleExoPlayer = null;
        trackSelector = null;
        exoPlayerView.release();
    }

}
