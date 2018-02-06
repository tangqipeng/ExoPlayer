package cn.tqp.exoplayer.manager;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
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
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import cn.tqp.exoplayer.exoplayerui.ExoPlayerLoadControl;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerView;
import cn.tqp.exoplayer.entity.VideoInfo;
import cn.tqp.exoplayer.listener.EventLogger;

/**
 * Created by tangqipeng on 2018/1/25.
 */

public class ExoPlayerManager {

    private static final String TAG = "ExoPlayerManager";
    private Context mContext;
    private ExoPlayerView playerView;
    private SimpleExoPlayer exoPlayer;
    private List<VideoInfo> mVideoInfoList;
    private VideoInfo mVideoInfo;
    private MediaSource[] mediaSources;
    private MediaSource[] previewMediaSources;
    private TrackSelector trackSelector;
    private EventLogger mEventLogger;
    private ExoPlayerLoadControl mLoadControl;
    private DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private Handler mainHandler = new Handler();

    public ExoPlayerManager(Context context, ExoPlayerView playerView) {
        this.mContext = context;
        this.playerView = playerView;
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        mLoadControl = new ExoPlayerLoadControl(context);
        mEventLogger = new EventLogger((MappingTrackSelector)trackSelector, playerView);
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
     * 预览窗口是以图片的形式
     * @param videoInfoList
     */
    public void addVideoDatasAndPreviewImages(List<VideoInfo> videoInfoList){
        this.mVideoInfoList = videoInfoList;
        playerView.setVideoInfoList(videoInfoList);
        mediaSources = new MediaSource[mVideoInfoList.size()];
        for (int i = 0; i < mVideoInfoList.size(); i ++){
            mediaSources[i] = addPlayMediaSouce(Uri.parse(mVideoInfoList.get(i).movieUrl));
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
                && mediaSources.length > 0){
            exoPlayer = createFullPlayer();
            playerView.setPlayer(exoPlayer);
            if (previewMediaSources != null && previewMediaSources.length > 0) {
                ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(previewMediaSources);
                playerView.addPreviewMovieUrl(concatenatedSource);
            }else{
                playerView.addPreviewImagesUrl(mVideoInfoList);
            }
            playerView.setMovieTitle(mVideoInfoList.get(0).movieTitle);
        }
    }

    private SimpleExoPlayer createFullPlayer() {
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(playerView.getContext()), trackSelector, mLoadControl);
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(mediaSources);
        player.prepare(concatenatedSource);
        player.setPlayWhenReady(true);
        player.addVideoDebugListener(mEventLogger);
        player.addMetadataOutput(mEventLogger);
        player.addListener(mEventLogger);
        return player;
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

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    public DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(mContext, "ExoPlayer"), bandwidthMeter);
    }

}
