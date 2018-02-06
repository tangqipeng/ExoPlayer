package cn.tqp.exoplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import cn.tqp.exoplayer.manager.ExoPlayerManager;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerView;
import cn.tqp.exoplayer.entity.PreviewImage;
import cn.tqp.exoplayer.entity.VideoInfo;
import cn.tqp.exoplayer.utils.ScreenUtils;

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
        //竖屏
        getSupportActionBar().hide();
        //横屏
//        ExoPlayerUtils.hideActionBarAndBottomUiMenu(this);

        //常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        exoPlayerView = (ExoPlayerView) findViewById(R.id.exoplayerview);

        //横屏
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//                if (android.os.Build.VERSION.SDK_INT > 18) {
//                    ((ViewGroup) exoPlayerView.getParent()).setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.INVISIBLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//                } else {
//                    ((ViewGroup) exoPlayerView.getParent()).setSystemUiVisibility(View.INVISIBLE);
//                }
//            }
//        });

        //竖屏加入这个
        int playerWidth = ScreenUtils.getScreenWidth(this);
        int playerHeight = playerWidth * 9 / 16;
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, playerHeight);
        exoPlayerView.setLayoutParams(layoutParams);

        //--------------------------------------------------------------------------
        //这句绝对不能少（再有大小屏切换的时候，不需要大小屏切换可以不设置）最好是每次都设置
        exoPlayerView.setExoPlayerViewContainer((ViewGroup) exoPlayerView.getParent());

        exoPlayerManager = new ExoPlayerManager(this, exoPlayerView);

        /**
         * 这里说明 如果采用的预览窗口的资源是个播放地址，那么 videoInfo.moviePreviewUrl 就是播放地址，注入数据的方法是exoPlayerManager.addVideoDatas(videoInfos)；
         * 如果预览窗口的资源是图片地址，那么videoInfo.moviePreviewUrl就是图片的地址，注入数据的方式是exoPlayerManager.addVideoDatasAndPreviewImages(videoInfos)
         */
        List<VideoInfo> videoInfos = new ArrayList<>();

//        VideoInfo videoInfo = new VideoInfo();
//        videoInfo.movieId = "1";
//        videoInfo.movieTitle = "银河护卫队";
//        videoInfo.movieUrl = "http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8";
//        videoInfo.moviePreviewUrl = "http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8";
//        videoInfo.imageCount = 42;
//        List<PreviewImage> previewImages = new ArrayList<>();
//        PreviewImage previewImage = new PreviewImage();
//        previewImage.imagePreviewUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg";
//        previewImage.imageSize = 42;
//        previewImage.lines = 7;
//        previewImage.colums = 7;
//        previewImages.add(previewImage);
//        videoInfo.previewImagesList = previewImages;
//        videoInfos.add(videoInfo);

//        VideoInfo videoInfo1 = new VideoInfo();
//        videoInfo1.movieId = "2";
//        videoInfo1.movieTitle = "TCL";
//        videoInfo1.movieUrl = getResources().getString(R.string.url_hls1);
//        videoInfo1.moviePreviewUrl = getResources().getString(R.string.url_hls1);
//        videoInfo1.imageCount = 42;
//        List<PreviewImage> previewImages1 = new ArrayList<>();
//        PreviewImage previewImage1 = new PreviewImage();
//        previewImage1.imagePreviewUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg";
//        previewImage1.imageSize = 42;
//        previewImage1.lines = 7;
//        previewImage1.colums = 7;
//        previewImages1.add(previewImage1);
//        videoInfo1.previewImagesList = previewImages1;
//        videoInfos.add(videoInfo1);

        VideoInfo videoInfo2 = new VideoInfo();
        videoInfo2.movieId = "2";
        videoInfo2.movieTitle = "怪奇物语";
        videoInfo2.movieUrl = "http://pumpkin-online-movie-development.oss-cn-beijing.aliyuncs.com/zufudechengfa_iframe.m3u8";
        videoInfo2.moviePreviewUrl = "http://pumpkin-online-movie-development.oss-cn-beijing.aliyuncs.com/201801/PIQvhUGb/pqczrawfwM.m3u8";
        videoInfo2.imageCount = 42;
        List<PreviewImage> previewImages2 = new ArrayList<>();
        PreviewImage previewImage2 = new PreviewImage();
        previewImage2.imagePreviewUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg";
        previewImage2.imageSize = 42;
        previewImage2.lines = 7;
        previewImage2.colums = 7;
        previewImages2.add(previewImage2);
        videoInfo2.previewImagesList = previewImages2;
        videoInfos.add(videoInfo2);

//        exoPlayerManager.addVideoDatas(videoInfos);
        exoPlayerManager.addVideoDatasAndPreviewImages(videoInfos);

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
        exoPlayerManager.destroy();
    }
}
