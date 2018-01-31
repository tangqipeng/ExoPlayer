package cn.tqp.exoplayer.exoplayerui;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
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
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.tqp.exoplayer.DemoApplication;

/**
 * Created by tangqipeng on 2018/1/25.
 */

public class ExoPlayerManager {

    private static final String TAG = "ExoPlayerManager";
    private ExoPlayerView playerView;
    private SimpleExoPlayer exoPlayer;
    private List<VideoInfo> mVideoInfoList;
    private VideoInfo mVideoInfo;
    private MediaSource[] mediaSources;
    private MediaSource[] previewMediaSources;
    private TrackSelector trackSelector;
    private EventLogger mEventLogger;
    private LoadControl mLoadControl;
    private DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private Handler mainHandler = new Handler();

    public ExoPlayerManager(ExoPlayerView playerView) {
        this.playerView = playerView;
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        mEventLogger = new EventLogger((MappingTrackSelector)trackSelector, playerView);
        mLoadControl = new DefaultLoadControl();
    }

    /**
     * 一次注入多个数据
     * @param videoInfoList
     */
    public void addVideoDatas(List<VideoInfo> videoInfoList){
        this.mVideoInfoList = videoInfoList;
        playerView.setVideoInfoList(videoInfoList);
        mediaSources = new MediaSource[mVideoInfoList.size()];
        previewMediaSources = new MediaSource[mVideoInfoList.size()];
        for (int i = 0; i < mVideoInfoList.size(); i ++){
            mediaSources[i] = addPlayMediaSouce(Uri.parse(mVideoInfoList.get(i).movieUrl));
            previewMediaSources[i] = addPreviewMediaSouce(Uri.parse(mVideoInfoList.get(i).moviePreviewUrl));
        }
    }

    /**
     * 一次加入单个数据
     * @param videoInfo
     */
    public void addVideoData(VideoInfo videoInfo){
        this.mVideoInfo = videoInfo;
        mVideoInfoList = new ArrayList<>();
        mVideoInfoList.add(videoInfo);
        playerView.setVideoInfoList(mVideoInfoList);
        mediaSources = new MediaSource[1];
        previewMediaSources = new MediaSource[1];
        mediaSources[0] = addPlayMediaSouce(Uri.parse(mVideoInfo.movieUrl));
        previewMediaSources[0] = addPlayMediaSouce(Uri.parse(mVideoInfo.moviePreviewUrl));
    }

    public void onStart() {
        if (Util.SDK_INT > 23) {
            createPlayers();
        }
    }

    public void onResume() {
        if (Util.SDK_INT <= 23) {
            createPlayers();
        }
    }

    public void onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayers();
        }
    }

    public void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayers();
        }
    }

    private void releasePlayers() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
            playerView.release();
        }
    }

    public void destroy(){
        trackSelector = null;
        mLoadControl.onStopped();
        mLoadControl.onReleased();
        mLoadControl = null;
    }

    private void createPlayers() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (mVideoInfoList != null && mVideoInfoList.size() > 0 && mediaSources != null
                && mediaSources.length > 0 && previewMediaSources != null && previewMediaSources.length > 0){
            exoPlayer = createFullPlayer();
            playerView.setPlayer(exoPlayer);
            ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(previewMediaSources);
            playerView.addPreviewMovieUrl(concatenatedSource);
            playerView.setMovieTitle(mVideoInfoList.get(0).movieTitle);
        }
    }

    private SimpleExoPlayer createFullPlayer() {
//        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(playerView.getContext(), trackSelector);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(playerView.getContext()), trackSelector, mLoadControl);
        player.setPlayWhenReady(true);
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(mediaSources);
        player.prepare(concatenatedSource);
        player.addListener(mEventLogger);
        return player;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    public DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return DemoApplication.getInstance().buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new MediaSouce in player
     * @param uri
     * @return
     */
    public MediaSource addPlayMediaSouce(Uri uri){
        return buildMediaSource(uri, true, mEventLogger);
    }

    /**
     * Returns a new MediaSouce in Preview
     * @param uri Play address  of Minimum resolution
     * @return
     */
    public MediaSource addPreviewMediaSouce(Uri uri){
        return buildMediaSource(uri, false, null);
    }

    public MediaSource buildMediaSource(Uri uri, boolean useBandwidthMeter, EventLogger eventLogger) {
        @C.ContentType
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false)).createMediaSource(uri, mainHandler, eventLogger);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri, mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

}
