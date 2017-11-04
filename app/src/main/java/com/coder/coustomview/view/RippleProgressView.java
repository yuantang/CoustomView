package com.coder.coustomview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.coder.coustomview.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * 1、根据播放显示进度
 * 2、向外扩散
 * 3、动态设置颜色、样式
 * 4、可控制开始结束
 */

public class RippleProgressView extends View {
    private Context context;
    /*默认的圆环画笔*/
    private Paint mRoundPaint;
    /*进度后的圆环画笔*/
    private Paint mProgressPaint;
    /*扩散圆环的画笔*/
    private Paint mRipplePaint;

    /*圆弧的范围*/
    private RectF rectF =new RectF();

    /*圆心X坐标*/
    private int mCenterX;
    /*圆心Y坐标*/

    private int mCenterY;
    /*圆环半径*/
    private float mRadius;

    private float mMaxRoundRadius = 0.7f;

    /*圆环宽度*/
    private float mRoundWidth;
    /*圆环颜色*/
    private int mRoundColor;
    /*进度条颜色*/
    private int mProgressColor;
    /*最大进度*/
    private int mProgressMax=100;

    /*水波纹颜色*/
    private int mRippleColor;
    /*水波纹宽度*/
    private float mRippleWidth;

    /*水波纹创建到消失时长*/
    private long mRippleDuration = 5000;
    /*波纹的创建速度，每1500ms创建一个*/
    private int mRippleSpeed = 1500;

    /*进度*/
    private int mProgress=0;

    /*初始波纹半径*/
    private float mInitialRadius;
    /*如果没有设置mMaxRadius，可mMaxRadius = 最小长度 * mMaxRadiusRate*/
    private float mMaxRadiusRate = 0.85f;

    /*最大波纹半径*/
    private float mMaxRadius;

    private Interpolator mInterpolator;
    private List<Circle> mCircleList = new ArrayList<>();
    private boolean mIsRunning;
    private long mLastCreateTime;
    private boolean mMaxRadiusSet=false;

    public RippleProgressView(Context context) {
        this(context,null);
    }

    public RippleProgressView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }
    public RippleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.RippleProgressView);
        mRoundColor=typedArray.getColor(R.styleable.RippleProgressView_roundColor,Color.LTGRAY);
        mRoundWidth=typedArray.getDimension(R.styleable.RippleProgressView_roundWidth,8);
        mProgressColor=typedArray.getColor(R.styleable.RippleProgressView_progressColor,Color.WHITE);
        mProgressMax=typedArray.getInteger(R.styleable.RippleProgressView_progressMax,100);
        mRippleColor=typedArray.getColor(R.styleable.RippleProgressView_rippleColor,Color.LTGRAY);
        mRippleWidth=typedArray.getDimension(R.styleable.RippleProgressView_rippleWidth,3);
        mRippleDuration=typedArray.getInteger(R.styleable.RippleProgressView_rippleDuration,5000);
        mRippleSpeed=typedArray.getInteger(R.styleable.RippleProgressView_rippleSpeed,1500);
        typedArray.recycle();
        init();
    }

    private void init() {
        mRoundPaint =new Paint();
        mRoundPaint.setAntiAlias(true);
        mRoundPaint.setColor(mRoundColor);
        mRoundPaint.setStyle(Paint.Style.STROKE);
        mRoundPaint.setStrokeWidth(mRoundWidth);

        mProgressPaint =new Paint();
        mRoundPaint.setAntiAlias(true);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mRoundWidth);

        mRipplePaint =new Paint(Paint.ANTI_ALIAS_FLAG);
        mRoundPaint.setAntiAlias(true);
        mRipplePaint.setColor(mRippleColor);
        mRipplePaint.setStyle(Paint.Style.STROKE);
        mRipplePaint.setStrokeWidth(mRippleWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX=w/2;
        mCenterY=h/2;
        int min=Math.min(mCenterX,mCenterY);

        mRadius= (int) ( min - mRoundWidth ) * mMaxRoundRadius;

        Log.e("","===mRadius==="+mRadius);

        mInitialRadius = mRadius + mRoundWidth;

        Log.e("","===mInitialRadius==="+mInitialRadius);

        if (!mMaxRadiusSet) {
            mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
        }
        rectF.set(mCenterX-mRadius,mCenterX-mRadius,mCenterX+mRadius,mCenterX+mRadius);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height=getDefaultSize(getSuggestedMinimumHeight(),heightMeasureSpec);
        int width=getDefaultSize(getSuggestedMinimumWidth(),widthMeasureSpec);
        int min=Math.min(width,height);
        setMeasuredDimension(min,min);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画默认圆环
        canvas.drawCircle(mCenterX,mCenterY,mRadius ,mRoundPaint);
        //画圆弧
        //绘制，设置进度条的度数从0开始，结束值是个变量，可以自己自由设置，来设置进度
        //true和false 代表是否使用中心点，如果true，代表连接中心点，会出现扇形的效果
        canvas.drawArc(rectF, -90, (float) (3.6 * mProgress), false, mProgressPaint);


        Iterator<Circle> iterator = mCircleList.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            if (System.currentTimeMillis() - circle.mCreateTime < mRippleDuration) {
                mRipplePaint.setAlpha(circle.getAlpha());
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, circle.getCurrentRadius(), mRipplePaint);
            } else {
                iterator.remove();
            }
        }
        if (mCircleList.size() > 0) {
            postInvalidateDelayed(10);
        }
    }
    private Runnable mCreateCircle = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                newCircle();
                postDelayed(mCreateCircle, mRippleSpeed);
            }
        }
    };



    /**
     * 开始
     */
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mCreateCircle.run();
        }
    }

    /**
     * 停止
     */
    public void stop() {
        mIsRunning = false;
    }

    /**
     * 设置扩散的插值器
     * @param interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
        if (mInterpolator == null) {
            mInterpolator = new LinearInterpolator();
        }
    }

    public void setProgress(int mProgress) {
        if (mProgress==mProgressMax){
            this.mProgress=0;
            stop();
        }else {
            this.mProgress = mProgress;
        }

        postInvalidate();
    }
    public int getProgress(){
        return this.mProgress;
    }

    private void newCircle() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCreateTime < mRippleSpeed) {
            return;
        }
        Circle circle = new Circle();
        mCircleList.add(circle);
        invalidate();
        mLastCreateTime = currentTime;
    }

    private class Circle {
        private long mCreateTime;
        public Circle() {
            this.mCreateTime = System.currentTimeMillis();
        }
        public int getAlpha() {
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mRippleDuration;
            return (int) ((1.0f - mInterpolator.getInterpolation(percent)) * 255);
        }
        public float getCurrentRadius() {
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mRippleDuration;
            return mInitialRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mInitialRadius);
        }
    }
}
