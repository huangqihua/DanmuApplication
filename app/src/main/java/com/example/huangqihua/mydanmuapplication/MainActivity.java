package com.example.huangqihua.mydanmuapplication;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private boolean showDanmaku;

    private VideoView mVideoView;

    private DanmakuView mDanmakuView;

    private DanmakuContext mDanmakuContext;

    private LinearLayout mOperationLayout;
    private EditText mEditText;

    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {

        mVideoView = (VideoView) findViewById(R.id.video_view);
        //手机跟目录下xx.mp4
        mVideoView.setVideoPath(Environment.getExternalStorageDirectory() + "/sofia.mp4");
        mVideoView.start();

        mDanmakuView = (DanmakuView) findViewById(R.id.dan_mu_view);
        mDanmakuView.enableDanmakuDrawingCache(true);
        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                mDanmakuView.start();
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });


        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行

        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);


        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
//                .setMaximumLines(maxLinesPair)
                .setCacheStuffer(new BackgroundCacheStuffer(), null)
                .preventOverlapping(overlappingEnablePair);

        mDanmakuView.prepare(parser, mDanmakuContext);

        mOperationLayout = (LinearLayout) findViewById(R.id.operation_layout);
        mEditText = (EditText) findViewById(R.id.edit_text);


        mDanmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOperationLayout.getVisibility() == View.GONE) {
                    mOperationLayout.setVisibility(View.VISIBLE);
                } else {
                    mOperationLayout.setVisibility(View.GONE);
                }
            }
        });

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    onWindowFocusChanged(true);
                }
            }
        });

    }

    /**
     * 随机生成一些弹幕
     */
    private void generateSomeDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (showDanmaku) {
                    int time = new Random().nextInt(300);
                    String content = "" + time + time;
                    adddDanmaku(content, false);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    /**
     * 向弹幕view添加一条弹幕
     *
     * @param content     弹幕内容
     * @param widthBorder
     */
    private void adddDanmaku(String content, boolean widthBorder) {
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textSize = sp2px(20);
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(mDanmakuView.getCurrentTime());
        if (widthBorder) {
            danmaku.borderColor = Color.RED;
        }
        mDanmakuView.addDanmaku(danmaku);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        showDanmaku = false;
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //沉浸式状态栏
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * 发送弹幕
     *
     * @param view
     */
    public void send(View view) {
        String content = mEditText.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            adddDanmaku(content, true);
            mEditText.setText("");
        }
    }


    /**
     * 绘制背景(自定义弹幕样式)
     */
    private class BackgroundCacheStuffer extends SpannedCacheStuffer {
        // 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
        final Paint paint = new Paint();

        @Override
        public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
            danmaku.padding = 10;  // 在背景绘制模式下增加padding
            super.measure(danmaku, paint, fromWorkerThread);
        }

        @Override
        public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {
            paint.setColor(0x8125309b);
            canvas.drawRect(left + 2, top + 2, left + danmaku.paintWidth - 2, top + danmaku.paintHeight - 2, paint);
        }

        @Override
        public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint) {
            // 禁用描边绘制
        }
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
