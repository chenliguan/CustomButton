package com.guan.custombutton;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Guan
 * @file com.guan.custombutton
 * @date 2015/11/10
 * @Version 1.0
 */
public class CustomButton extends View implements View.OnClickListener {

    /*
 * 自定义view的几个步骤：
 * 1、首先需要写一个类来继承自View(或者View的子类)
 * 2、需要得到view的对象，那么需要重写构造方法，其中一参的构造方法用于new，二参的构造方法用于xml布局文件使用，三参的构造方法可以传入一个样式
 * 3、需要设置view的大小，那么需要重写onMeasure方法
 * 4、需要设置view的位置，那么需要重写onLayout方法，但是这个方法在自定义view的时候用的不多，原因主要在于view的位置主要是由父控件来决定
 * 5、需要绘制出所需要显示的view，那么需要重写onDraw方法
 * 6、当控件状态改变的时候，需要重绘view，那么调用invalidate();方法，这个方法实际上会重新调用onDraw方法
 * 7、在这其中，如果需要对view设置点击事件，可以直接调用setOnClickListener方法
 * 8、需要实现触摸拖拽功能，那么需要重写的onTouchEvent方法，基本上是处理ACTION_DOWN、ACTION_MOVE和ACTION_UP事件
 */

    // 背景图片
    private Bitmap mBackgroudBitmap;
    // 开关图片
    private Bitmap mSlidingBitmap;
    // 开关是否打开
    private boolean mIsOpen;
    // 是否可点击
    private boolean mIsClickable;
    // 最大偏移量（离控件左边的距离）
    private int mSlidingMaxOffset;
    // 当前偏移量
    private int mSlidingCurrentOffset;
    // 开始X坐标
    private float mStartX;
    // 移动的X坐标
    private float mMoveX;

    public CustomButton(Context context) {
        super(context);
    }

    /**
     * 构造方法（两个参数，必须重写）
     *
     * @param context
     * @param attrs
     */
    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        // 得到背景图片转化为bitmap对象
        mBackgroudBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.a);
        // 得到button图片转化为bitmap对象
        mSlidingBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.b);
        // 计算最大偏移量（=背景图宽 - 按钮宽）
        mSlidingMaxOffset = mBackgroudBitmap.getWidth() - mSlidingBitmap.getWidth();
        // 给自定义的view设置点击事件
        setOnClickListener(this);
    }

    /**
     * 测量指定图片如何显示(如何展示给用户)
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 指定背景如何显示
        setMeasuredDimension(mBackgroudBitmap.getWidth(), mBackgroudBitmap.getHeight());
    }

    /**
     * @param canvas
     */
    @Override
    public void onDraw(Canvas canvas) {
        // 画笔工具
        Paint paint = new Paint();
        // 初始化一个画笔
        paint = new Paint();
        // 设置抗锯齿
        paint.setAntiAlias(true);
        // 1.画出背景图片
        canvas.drawBitmap(mBackgroudBitmap, 0, 0, paint);
        // 2.画出button按钮(开始位置：当前偏移量，实时更新)
        canvas.drawBitmap(mSlidingBitmap, mSlidingCurrentOffset, 0, paint);
    }

    /**
     * 给自定义的view设置点击事件
     *
     * onClick和onLongClick是在super.onTouchEvent方法里被调用的，onClick是在ACTION_UP的时候可能被调用，而onLongClick是在ACTION_DOWN的时候可能被调用。
     */
    @Override
    public void onClick(View v) {
        if (mIsClickable) {
            // 如何是打开的
            if (mIsOpen) {
                mSlidingCurrentOffset = 0;
            } else {
                mSlidingCurrentOffset = mSlidingMaxOffset;
            }
            mIsOpen = !mIsOpen;
            // 重新初始化界面，可是OnDraw再次执行
            flushView();
        }
    }

    /**
     * 触摸监听
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 1.记录第一次触摸时按下的X坐标
                mStartX = event.getX();
                // 按下时恢复开关的点击事件
                mIsClickable = true;
                break;

            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getX();
                // 如果有移动距离，就视为是触摸，就屏蔽点击事件
                if (Math.abs(mMoveX - mStartX) > 0) {
                    mIsClickable = false;
                }
                // 得到最新的当前距离
                mSlidingCurrentOffset += (int) (mMoveX - mStartX);
                // 做一个判断，防止滑块划出边界，滑块的范围应该是在[0,mSlidingMaxOffset];
                if (mSlidingCurrentOffset > mSlidingMaxOffset)
                    mSlidingCurrentOffset = mSlidingMaxOffset;
                if (mSlidingCurrentOffset < 0)
                    mSlidingCurrentOffset = 0;
                // 重新初始化界面
                flushView();
                // 还原，最新的开始X坐标
                mStartX = event.getX();
                break;

            case MotionEvent.ACTION_UP:
                // 抬起的时候，判断松开的位置是哪，来决定开关的状态是打开还是关闭
                if (mSlidingCurrentOffset > mSlidingMaxOffset / 2)
                    mSlidingCurrentOffset = mSlidingMaxOffset;
                if (mSlidingCurrentOffset <= mSlidingMaxOffset / 2)
                    mSlidingCurrentOffset = 0;
                // 重新初始化界面
                flushView();
                break;
        }
        // 重新初始化界面
        flushView();

        return true;
    }

    /**
     * 刷新视图
     */
    protected void flushView() {
        // 刷新当前view会导致ondraw方法的执行
        invalidate();
    }
}
