package cn.tqp.exoplayer;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.List;

import cn.tqp.exoplayer.exoplayer.ExoPlayerMediaSourceBuilder;

/**
 * Created by tangqipeng on 2018/1/24.
 */

public class ExoplayerManager implements Player.EventListener, TimeBar.OnScrubListener {

    private SimpleExoPlayerView playerView;
    private SimpleExoPlayerView previewPlayerView;
    private SimpleExoPlayer player;
    private SimpleExoPlayer previewPlayer;
    private FrameLayout mFrameLayout;
    private DefaultTimeBar defaultTimeBar;
    private String mMovieUrl;

    private boolean mIsConcatenatingMediaSource = false;

    private MediaSouceListener mediaSouceListener;
    private Handler mainHandler = new Handler();

    private static DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public ExoplayerManager(SimpleExoPlayerView playerView, SimpleExoPlayerView previewPlayerView, FrameLayout frameLayout, DefaultTimeBar timeBar) {
        this.playerView = playerView;
        this.previewPlayerView = previewPlayerView;
        this.mFrameLayout = frameLayout;
        this.defaultTimeBar = timeBar;
        mediaSouceListener = new MediaSouceListener();
    }

    public void addPlayUrl(String movieUrl) {
        this.mMovieUrl = movieUrl;
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

    public void setConcatenatingMediaSource(boolean isConcatenatingMediaSource){
        this.mIsConcatenatingMediaSource = isConcatenatingMediaSource;
    }

    private void releasePlayers() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (previewPlayer != null) {
            previewPlayer.release();
            previewPlayer = null;
        }
    }

    private void createPlayers() {
        if (player != null) {
            player.release();
        }
        if (previewPlayer != null) {
            previewPlayer.release();
        }

        defaultTimeBar.addListener(this);

        player = createFullPlayer();
        playerView.setPlayer(player);
        if (!mIsConcatenatingMediaSource){
            previewPlayer = createPreviewPlayer();
            previewPlayerView.setPlayer(previewPlayer);
        }
    }

    private SimpleExoPlayer createFullPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(playerView.getContext(), trackSelector);
        player.setPlayWhenReady(true);
        if (!mIsConcatenatingMediaSource){
            MediaSource source = buildMediaSource(Uri.parse(mMovieUrl), true, mediaSouceListener);
            player.prepare(source);
        }else{
//            MediaSource[] mediaSources;
//
//            ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(mediaSources);
//            player.prepare(concatenatedSource);
        }

        player.addListener(this);
        return player;
    }

    private SimpleExoPlayer createPreviewPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(null);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(previewPlayerView.getContext(),trackSelector);
        player.setPlayWhenReady(false);
        player.setVolume(0f);
        MediaSource source = buildMediaSource(Uri.parse(mMovieUrl), false, null);
        player.prepare(source);
        return player;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private static DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return DemoApplication.getInstance().buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private MediaSource buildMediaSource(Uri uri, boolean useBandwidthMeter, @Nullable MediaSourceEventListener listener) {
        @C.ContentType
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false)).createMediaSource(uri, mainHandler, listener);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(buildDataSourceFactory(useBandwidthMeter)),
                        buildDataSourceFactory(false))
                        .createMediaSource(uri, mainHandler, listener);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, mainHandler, listener);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(buildDataSourceFactory(useBandwidthMeter))
                        .createMediaSource(uri, mainHandler, listener);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_READY && playWhenReady) {
//            seekBarLayout.hidePreview();
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        mFrameLayout.setVisibility(View.VISIBLE);
        previewPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
        previewPlayer.seekTo((long) position);
        previewPlayer.setPlayWhenReady(false);
        View view = previewPlayerView.getVideoSurfaceView();
        if (view instanceof SurfaceView) {
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        player.setPlayWhenReady(true);
        View view = previewPlayerView.getVideoSurfaceView();
        if (view instanceof SurfaceView) {
            view.setVisibility(View.INVISIBLE);
        }
        previewPlayer.setPlayWhenReady(false);
        mFrameLayout.setVisibility(View.INVISIBLE);
    }

    private class MediaSouceListener implements MediaSourceEventListener {

        @Override
        public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {

        }

        @Override
        public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

        }

        @Override
        public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

        }

        @Override
        public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {

        }

        @Override
        public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

        }

        @Override
        public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {

        }
    }

}
