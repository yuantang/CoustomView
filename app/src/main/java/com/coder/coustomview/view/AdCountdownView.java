package com.coder.coustomview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.coder.coustomview.R;


/**
 * 开屏广告倒计时控件
 */
public class AdCountdownView extends View {
    private float mMeasureTextWidth;
    private float mCircleDoubleRadius;
    private RectF mRectF;
    private Paint mArcPaint;
    private Paint mCirclePaint;
    private Paint mTextPaint;
    private Handler mHandler;
    int mCenterX;
    int mCenterY;
    public float currentTime = 0;
    //广告页面跳转的总时间
    private int countTotalTime;
    //刷新的时长
    private int refreshTime;
    //圆的颜色
    private int circleColor;
    //外部圆弧的颜色
    private int roundColor;
    //外部圆弧的笔画宽度
    private static int roundWidth;
    //中间文字的颜色
    private int textColor;
    //中间文字的大小
    private static int textSize;


    public AdCountdownView(Context context) {
        this(context,null);
    }
    public AdCountdownView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public AdCountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.AdCountdownView);
        circleColor=typedArray.getColor(R.styleable.AdCountdownView_circleColor,Color.BLACK);
        roundColor=typedArray.getColor(R.styleable.AdCountdownView_roundColor,Color.RED);
        textColor=typedArray.getColor(R.styleable.AdCountdownView_textColor,Color.WHITE);
        refreshTime=typedArray.getInt(R.styleable.AdCountdownView_refreshTime,1000);
        countTotalTime=typedArray.getInt(R.styleable.AdCountdownView_countTime,3000);
        roundWidth=typedArray.getDimensionPixelOffset(R.styleable.AdCountdownView_roundWidth,8);
        textSize=typedArray.getDimensionPixelSize(R.styleable.AdCountdownView_textSize,24);
        typedArray.recycle();
        init();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX=w/2;
        mCenterY=h/2;
        int min=Math.min(mCenterX,mCenterY);
        //计算中间的圆的直径
        mCircleDoubleRadius = min- roundWidth;
        //准备一个矩形出来,画圆弧
        mRectF = new RectF(mCenterX-mCircleDoubleRadius,mCenterX-mCircleDoubleRadius,
                mCenterX+mCircleDoubleRadius,mCenterX+mCircleDoubleRadius);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height=getDefaultSize(getSuggestedMinimumHeight(),heightMeasureSpec);
        int width=getDefaultSize(getSuggestedMinimumWidth(),widthMeasureSpec);
        int min=Math.min(width,height);
        setMeasuredDimension(min,min);
    }

    private void init() {
        //文字的画笔
        mTextPaint = new Paint();
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);

        //中间的圆的画笔
        mCirclePaint = new Paint();
        mCirclePaint.setColor(circleColor);
        mCirclePaint.setAntiAlias(true);

        //外部圆弧的画笔
        mArcPaint = new Paint();
        mArcPaint.setColor(roundColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(roundWidth);

        //利用handler实现循环 自己给自己发消息
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //接收到消息,会去更新一下角度,重绘一下
                currentTime += refreshTime;
                //当角度百分百了,就不再发消息了
                if (currentTime > countTotalTime) {
                    mHandler.removeCallbacksAndMessages(null);
                    //跳转页面
                    if (mOnSkipListener != null) {
                        mOnSkipListener.onSkip();
                    }
                    return;
                }
                invalidate();
                //继续发延时消息给自己
                this.sendEmptyMessageDelayed(0, refreshTime);
            }
        };
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //中间的圆
        canvas.drawCircle(mCenterX, mCenterX, mCircleDoubleRadius, mCirclePaint);
        //内部的文字
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        float angle =(currentTime / countTotalTime)  * 360;
        canvas.drawArc(mRectF, 0, angle, false, mArcPaint);
        int  duTime= (int) ((countTotalTime -currentTime)/1000);
        String duTimes;
        duTimes=duTime+"秒";
        mMeasureTextWidth = mTextPaint.measureText(duTimes);
        canvas.drawText(duTimes , mCenterX - mMeasureTextWidth / 2,mCenterY-((top + bottom) / 2), mTextPaint);

    }

    //找到该控件调用该方法开始发消息
    public void start() {
        mHandler.sendEmptyMessageDelayed(0, refreshTime);
    }

    //停止发消息的方法
    public void stop() {
        mHandler.removeCallbacksAndMessages(null);
    }

    //给空间
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                setAlpha(0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                //让handler的还未处理的消息被移除
                mHandler.removeCallbacksAndMessages(null);
                setAlpha(1f);
                if (mOnSkipListener != null) {
                    mOnSkipListener.onSkip();
                }
                break;
        }
        return true;
    }

    private OnSkipListener mOnSkipListener;
    public void setOnSkipListener(OnSkipListener listener) {
        mOnSkipListener = listener;
    }
    public interface OnSkipListener {
        void onSkip();
    }

}