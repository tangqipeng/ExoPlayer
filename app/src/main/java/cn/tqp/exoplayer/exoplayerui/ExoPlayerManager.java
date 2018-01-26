package cn.tqp.exoplayer.exoplayerui;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
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
    private EventLogger eventLogger;
    private DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private Handler mainHandler = new Handler();
    private MediaSouceListener mediaSouceListener = new MediaSouceListener();

    public ExoPlayerManager(ExoPlayerView playerView) {
        this.playerView = playerView;
    }

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

    public void addVideoData(VideoInfo videoInfo){
        this.mVideoInfo = videoInfo;
        List<VideoInfo> videoInfos = new ArrayList<>();
        videoInfos.add(videoInfo);
        playerView.setVideoInfoList(videoInfos);
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
            exoPlayer.release();
            exoPlayer = null;
            playerView.release();
        }
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
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(playerView.getContext(), trackSelector, loadControl);
        eventLogger = new EventLogger(trackSelector);
        player.setPlayWhenReady(true);
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(mediaSources);
        player.prepare(concatenatedSource);
        player.addListener(eventLogger);
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
        return buildMediaSource(uri, true);
    }

    /**
     * Returns a new MediaSouce in Preview
     * @param uri Play address  of Minimum resolution
     * @return
     */
    public MediaSource addPreviewMediaSouce(Uri uri){
        return buildMediaSource(uri, false);
    }

    public MediaSource buildMediaSource(Uri uri, boolean useBandwidthMeter) {
        @C.ContentType
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false)).createMediaSource(uri, mainHandler, mediaSouceListener);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri, mainHandler, mediaSouceListener);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, mainHandler, mediaSouceListener);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, mainHandler, mediaSouceListener);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private static class MediaSouceListener implements MediaSourceEventListener {

        @Override
        public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {
            Log.d(TAG, "onLoadStarted dataSpec [" + dataSpec.uri + "] dataType [" + dataType + "] trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" +
                    trackSelectionReason + "] trackSelectionData [" + trackSelectionData + "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "] elapsedRealtimeMs [" + elapsedRealtimeMs + "]");
        }

        @Override
        public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
            Log.d(TAG, "onLoadCompleted dataSpec [" + dataSpec.uri + "] dataType [" + dataType + "] trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" + trackSelectionReason + "] trackSelectionData [" + trackSelectionData +
                    "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "] elapsedRealtimeMs [" + elapsedRealtimeMs + "] loadDurationMs [" + loadDurationMs + "] bytesLoaded [" + bytesLoaded + "]");
        }

        @Override
        public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
            Log.d(TAG, "onLoadCanceled dataSpec [" + dataSpec.uri + "] dataType [" + dataType + "] trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" + trackSelectionReason + "] trackSelectionData [" + trackSelectionData +
                    "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "] elapsedRealtimeMs [" + elapsedRealtimeMs + "] loadDurationMs [" + loadDurationMs + "] bytesLoaded [" + bytesLoaded + "]");
        }

        @Override
        public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
            Log.d(TAG, "onLoadError dataSpec [" + dataSpec.uri + "] dataType [" + dataType + "] trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason [" + trackSelectionReason + "] trackSelectionData [" + trackSelectionData +
                    "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs [" + mediaEndTimeMs + "] elapsedRealtimeMs [" + elapsedRealtimeMs + "] loadDurationMs [" + loadDurationMs + "] bytesLoaded [" + bytesLoaded + "] error [" + error.getMessage() + "] wasCanceled [" + wasCanceled + "]");
        }

        @Override
        public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {
            Log.e(TAG, "onUpstreamDiscarded trackType [" + trackType + "] mediaStartTimeMs [" + mediaStartTimeMs + "] mediaEndTimeMs ["+ mediaEndTimeMs + "]");
        }

        @Override
        public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {
            Log.e(TAG, "onDownstreamFormatChanged trackType [" + trackType + "] trackFormat [" + trackFormat + "] trackSelectionReason ["+ trackSelectionReason
                    + "] trackSelectionData [" + trackSelectionData + "] mediaTimeMs ["+ mediaTimeMs + "]");
        }
    }

}
