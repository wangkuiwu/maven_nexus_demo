package com.skw.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Region;
import android.text.SpannableString;   
import android.text.Spanned;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 温度趋势图
 *
 * @author skywang
 * @e-mail kuiwu-wang@163.com
 */
public class TrendView extends View {
    private static final String TAG = "##skywang-TrendView";

    // 字体在圆圈之上
    public static final int DIRECTION_UP   = 0;
    // 字体在圆圈之下
    public static final int DIRECTION_DOWN = 1;

    // 圆点对应的画笔的宽度
    private static final int STROKE_WIDTH = 3;
    // 圆点的半径
    private static final int RADIUS = 7;
    // 圆环的大小
    private static final int RING_SIZE = 2;
    // 字体大小
    private static final int FONT_SIZE = 12;
    // 商标大小
    private static final int UPPER_SIZE = 10;
    // 上标对应的文本
    private static final String TEMPERATURE_UPPER = "o";

    private static final int FONT_COLOR          = 0xff97c6ae;
    private static final int CIRCLE_COLOR_NORMAL = 0xff63bf91;

    private static final int DEFAULT_MIN_Y = 0;
    private static final int DEFAULT_MAX_Y = 100;

    private int mFontDirection = DIRECTION_UP;

    private int mMinY = DEFAULT_MIN_Y;
    private int mMaxY = DEFAULT_MAX_Y;

    // "最上面的顶点的中心"距离顶部的偏移
    private int mTopOffset;
    // "最下面的顶点的中心"距离顶部的偏移
    private int mBottomOffset;
    // 顶点半径的大小
    private int mRadius;
    // 圆环的大小
    private int mRingSize;
    // 上标的大小
    private int mUpperSize;
    // 字体的大小
    private int mFontSize;
    // 画笔的宽度
    private int mStrokeWidth;

    private int mIndex = 0;

    // 所有圆圈的路径
    private Path mCirclePath = new Path();

    // 数据的数值
    private ArrayList<Integer> mDataList = new ArrayList<Integer>();
    // 数据的坐标
    private ArrayList<Point> mFontPositions = new ArrayList<Point>();
    // 数据的上标的坐标
    private ArrayList<Point> mUpperPositions = new ArrayList<Point>();
    // 数据的数值所在圆圈的坐标
    private ArrayList<Point> mCirclePositions = new ArrayList<Point>();

    // 线段的Paint
    private Paint mLinePaint;
    // 圆圈的Paint
    private Paint mCirclePaint;
    private Paint mInnerPaint;
    // 字体数据的Paint
    private Paint mFontPaint;
    // 字体数据的Paint
    private Paint mUpperPaint;

    public TrendView(Context context) {
        this(context, null);
    }

    public TrendView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TrendView);
        mFontDirection = a.getInt(R.styleable.TrendView_font_direction, DIRECTION_UP);
        a.recycle();

        init();
    }

    private void init() {
        Context context = getContext();
        mStrokeWidth = dp2px(context, STROKE_WIDTH);
        mFontSize = dp2px(context, FONT_SIZE);
        mUpperSize = dp2px(context, UPPER_SIZE);
        mRadius = dp2px(context, RADIUS);
        mRingSize = dp2px(context, RING_SIZE);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(mStrokeWidth);
        mCirclePaint.setColor(CIRCLE_COLOR_NORMAL);
        mCirclePaint.setStyle(Style.STROKE);  // Style.STROKE是空心, Style.FILL是实心

        mInnerPaint = new Paint();
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setStrokeWidth(mStrokeWidth);
        mInnerPaint.setColor(CIRCLE_COLOR_NORMAL);
        mInnerPaint.setStyle(Style.FILL);  // Style.STROKE是空心, Style.FILL是实心


        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(mStrokeWidth);
        mLinePaint.setColor(CIRCLE_COLOR_NORMAL);

        mFontPaint = new Paint();
        mFontPaint.setAntiAlias(true);
        mFontPaint.setTextSize(mFontSize);
        mFontPaint.setColor(FONT_COLOR);

        mUpperPaint = new Paint();
        mUpperPaint.setAntiAlias(true);
        mUpperPaint.setTextSize(mUpperSize);
        mUpperPaint.setColor(FONT_COLOR);

        setOffset();
    }

    private int dp2px(Context context, float dp) { 
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f); 
    }

    private void setOffset() {
        if (mFontDirection == DIRECTION_DOWN) {
            mTopOffset = mRadius + mStrokeWidth;
            mBottomOffset = (int)Math.ceil(getFontHeight(mFontPaint)) 
                + mRadius + mStrokeWidth;
        } else {
            mTopOffset = (int)Math.ceil(getFontHeight(mFontPaint)) 
                + (int)Math.ceil(getFontHeight(mUpperPaint, TEMPERATURE_UPPER))/2 
                + mRadius + mStrokeWidth;
            mBottomOffset = mRadius + mStrokeWidth;
        }
    }

    /**
     * 设置字体的位置
     *
     * @param direction 0，表示字体显示在圆圈上面
     * @param direction 1，表示字体显示在圆圈下面
     */
    public void setFontDirection(int direction) {
        mFontDirection = (direction==DIRECTION_DOWN) 
            ? DIRECTION_DOWN : DIRECTION_UP;
        setOffset();
    }

    /**
     * 设置数据
     */
    public void setData(int[] array) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int value:array) {
            list.add(value);
        }

        setData(list);
    }

    public void setData(ArrayList<Integer> list) {
        mDataList = (ArrayList<Integer>)list.clone();

        // 获取最大/最小值
        ArrayList<Integer> tmpList = (ArrayList<Integer>)list.clone();
        Collections.sort(tmpList);
        mMinY = tmpList.get(0);
        mMaxY = tmpList.get(tmpList.size()-1);
        requestLayout();
        invalidate();
    }

    /**
     * 设置当前选项
     */
    public void setCurrentIndex(int index) {
        if (mIndex==index && index<0 || index>mDataList.size()-1) {
            return ;
        }

        mIndex = index;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mDataList.size()==0 || mMaxY<=mMinY) {
            return ;
        }
        int tOffset = getPaddingTop() + mTopOffset;             // 顶部偏移
        int bOffset = getPaddingBottom() + mBottomOffset;       // 底部偏移
        int yRatio = (getMeasuredHeight() - (tOffset+bOffset)) / (mMaxY - mMinY);
        int itemWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / mDataList.size();
        FontMetrics textMetrics = mFontPaint.getFontMetrics();
        // 计算上标"o"的大小
        int upperHeight = (int)Math.ceil(getFontHeight(mUpperPaint, TEMPERATURE_UPPER));

        mCirclePositions.clear();
        mFontPositions.clear();
        mUpperPositions.clear();
        mCirclePath.reset();
        for (int i=0; i<mDataList.size(); i++) {
            // 圆
            int x = itemWidth*i + itemWidth/2;
            int y = tOffset + (mMaxY-mDataList.get(i))*yRatio;
            mCirclePositions.add(new Point(x, y));
            // 将圆添加在mCirclePath中
            mCirclePath.addCircle(x, y, mRadius, Path.Direction.CW);

            // 数据文本你
            String text = String.valueOf(mDataList.get(i));
            float fontWidth = mFontPaint.measureText(text);
            x = (int)(x-fontWidth/2);

            if (mFontDirection==DIRECTION_DOWN) {
                y = (int)(y+mRadius*2+mStrokeWidth*2);
            } else {
                y = (int)(y-(textMetrics.bottom - textMetrics.top));
            }
            mFontPositions.add(new Point(x, y)); 

            // 上标
            int upperX = (int)(x+fontWidth);
            int upperY = (int)(y-upperHeight/2);
            mUpperPositions.add(new Point(upperX, upperY));
        }
        mCirclePath.close();
    }

    @Override
    public void draw(Canvas canvas) {
        int N = mCirclePositions.size();

        // 绘制圆和文字
        for (int i=0; i < N; i++) {
            // 圆
            canvas.drawCircle(mCirclePositions.get(i).x, mCirclePositions.get(i).y, mRadius, mCirclePaint);
            if (i==mIndex) {
                canvas.drawCircle(mCirclePositions.get(i).x, mCirclePositions.get(i).y, mRadius-2, mInnerPaint);
            }

            // 文字
            String str = String.valueOf(mDataList.get(i));
            canvas.drawText(str, 0, str.length(), mFontPositions.get(i).x, mFontPositions.get(i).y, mFontPaint);
            canvas.drawText(TEMPERATURE_UPPER, 0, 1, mUpperPositions.get(i).x, mUpperPositions.get(i).y, mUpperPaint);
        }

        // 当前区域 = 整个画布 - 所有圆组成的区域。然后，在当前区域上绘制线段。
        canvas.clipPath(mCirclePath, Region.Op.DIFFERENCE);
        canvas.save();
        for (int i=0; i < N-1; i++) {
            // 线段
            canvas.drawLine(mCirclePositions.get(i).x, mCirclePositions.get(i).y, 
                    mCirclePositions.get(i+1).x, mCirclePositions.get(i+1).y, mLinePaint);
        }
        canvas.restore();
    }

    public float getFontHeight(Paint p) {
        return getFontHeight(p, "0");
    }     

    public float getFontHeight(Paint p, String text) {
        return getFontSize(p, text).height;
    }

    public float getFontWidth(Paint p, String text) {
        return getFontSize(p, text).width;
    }

    public Size getFontSize(Paint p, String text) {
        FontMetrics metrics = p.getFontMetrics();
        float width = p.measureText(text);
        float height = metrics.bottom - metrics.top;

        return new Size(width, height);
    }

    public static class Size {
        public float width;
        public float height;

        public Size(float width, float height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return "size: "+width+"x"+height;
        }
    }
}
