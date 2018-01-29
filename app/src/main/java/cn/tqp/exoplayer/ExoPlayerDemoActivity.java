package cn.tqp.exoplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import cn.tqp.exoplayer.exoplayerui.ExoPlayerManager;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerUtils;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerView;
import cn.tqp.exoplayer.exoplayerui.VideoInfo;

/**
 * Created by tangqipeng on 2018/1/25.
 */

public class ExoPlayerDemoActivity extends AppCompatActivity {

    private ExoPlayerView exoPlayerView;
    private ExoPlayerManager exoPlayerManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);
        getSupportActionBar().hide();

        //常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        exoPlayerView = (ExoPlayerView) findViewById(R.id.exoplayerview);

        int playerWidth = ExoPlayerUtils.getScreenWidth(this);

        int playerHeight = playerWidth * 9 / 16;

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, playerHeight);

        exoPlayerView.setLayoutParams(layoutParams);

        //这句绝对不能少（再有大小屏切换的时候，不需要大小屏切换可以不设置）最好是每次都设置
        exoPlayerView.setExoPlayerViewContainer((ViewGroup) exoPlayerView.getParent());

        exoPlayerManager = new ExoPlayerManager(exoPlayerView);

        List<VideoInfo> videoInfos = new ArrayList<>();
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.movieId = "1";
        videoInfo.movieTitle = "银河护卫队";
        videoInfo.movieUrl = "http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8";
        videoInfo.moviePreviewUrl = "http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8";
        videoInfos.add(videoInfo);

        VideoInfo videoInfo1 = new VideoInfo();
        videoInfo1.movieId = "2";
        videoInfo1.movieTitle = "TLC";
        videoInfo1.movieUrl = getResources().getString(R.string.url_hls1);
        videoInfo1.moviePreviewUrl = getResources().getString(R.string.url_hls1);
        videoInfos.add(videoInfo1);

        VideoInfo videoInfo2 = new VideoInfo();
        videoInfo2.movieId = "3";
        videoInfo2.movieTitle = "TLC";
        videoInfo2.movieUrl = getResources().getString(R.string.url_hls1);
        videoInfo2.moviePreviewUrl = getResources().getString(R.string.url_hls1);
        videoInfos.add(videoInfo2);

        VideoInfo videoInfo3 = new VideoInfo();
        videoInfo3.movieId = "4";
        videoInfo3.movieTitle = "啥电影";
        videoInfo3.movieUrl = "http://pumpkin-online-movie-development.oss-cn-beijing.aliyuncs.com/zufudechengfa_iframe.m3u8";
        videoInfo3.moviePreviewUrl = "http://pumpkin-online-movie-development.oss-cn-beijing.aliyuncs.com/zufudechengfa_iframe.m3u8";
        videoInfos.add(videoInfo3);

        exoPlayerManager.addVideoDatas(videoInfos);

    }

    @Override
    protected void onStart() {
        super.onStart();
        exoPlayerManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        exoPlayerManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayerManager.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoPlayerManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
