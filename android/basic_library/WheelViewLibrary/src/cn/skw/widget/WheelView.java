package cn.skw.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 滚动选择控件。
 * 
 *
 * 注意： 如果通过代码的方式使用WheelView，注意函数调用的优先顺序。
 *
 *      String[] strArray = {"1","2","3","4","5","6","7"};
 *      mWheelView = (WheelView)findViewById(R.id.wheel_view);
 *      // [可选] WheelView变化的监听函数。
 *      mWheelView.setOnWheelChangeListener(this);
 *      // [可选] 设置显示个数，默认是3个。
 *      mWheelView.setDisplayNumber(3);
 *      // [可选] 设置加载时的选中项，默认是第一项。
 *      //        如果使用该接口，则setCurrentItem() 必须在setDisplayNumber()之后调用才有效。
 *      mWheelView.setCurrentItem(4);
 *      // [必需] 设置WheelView的数据。必须在最后调用setDataArray()接口
 *      mWheelView.setDataArray((List<String>)Arrays.asList(strArray));
 * 
 * @author: skywang
 * @e-mail: kuiwu-wang@163.com
 */
public class WheelView extends ScrollView {
    private static final String TAG = "##skywang-WheelView";
    private static final boolean DEBUG = false;

    // WheelView中可见的TextView的数目 (必须是: 2*N+1)
    private static final int DISPLAY_NUMBER_MIN     = 1;
    private static final int DISPLAY_NUMBER_MAX     = 9;
    private static final int DISPLAY_NUMBER_DEFAULT = 3;
    // Scroll Check的频率(ms)
    private static final int CHECK_FREQUENCE = 50;
    // 选中字体的颜色
    private static final int SELECT_COLOR = 0xff45c01a;
    // 未选中字体的颜色
    private static final int UNSELECT_COLOR = 0xffaaaaaa;
    // 指示线 的颜色
    // private static final int INDICATOR_COLOR = 0x83cde6;
    private static final int INDICATOR_COLOR = 0xffe1e1e1;
    // 指示线 / 总长度的值
    private static final float INDICATOR_RATIO_DEFAULT = 1.0f;

    private int mSelectColor;
    private int mUnselectColor;
    private int mSelectSize    = 20; //24
    private int mUnselectSize  = 20;

    // 显示数： 当前可见的TextView数量
    private int mDisplayNum;
    // 偏移值； 可见的几个TextView中，选中TextView距离WheelView顶部的偏移值
    private int mOffset;
    // 选中索引(将偏移的TextView计算在内)：当前选择的TextView对应的索引
    private int mIndex;
    // 根据ScrollY 计算出来的索引
    private int mScrollPosition;

    // 偏移
    private int mScrollY;
    // 每一项TextView的高度
    private int mItemHeight;
    // 手指离开屏幕时的ScrollY的位置
    private int mInitialPosition;
    // 指示线的占宽比
    private float mIndicatorRatio;

    // 画线的Paint
    private Paint mLinePaint;
    // 数据队列
    private List<String> mDataList;
    // 线性布局
    private LinearLayout mContainer;
    // 线性布局中TextView的LayoutParams
    private LinearLayout.LayoutParams mTextViewLayoutParam;

    private OnWheelChangeListener mWheelChangeListener;

    // WheelView变化接口
    public interface OnWheelChangeListener {
        public void onSelected(int index, String value);
    }

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mDataList = new ArrayList<String>();
        mItemHeight = calculateItemHeight() + 20;

        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.VERTICAL);

        // 画线的Paint
        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(1.0f);

        // 获取自定义的属性(attrs.xml中)对应的TypedArray
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
        // 获取number属性对应的值
        int itemHeight = a.getDimensionPixelSize(R.styleable.WheelView_item_height, 0);
        int displayNumber = a.getInt(R.styleable.WheelView_display_number, DISPLAY_NUMBER_DEFAULT);
        int currentItem = a.getInt(R.styleable.WheelView_current_item, 0);
        int dataArrayId = a.getResourceId(R.styleable.WheelView_data_array, -1);
        int indicatorColor = a.getColor(R.styleable.WheelView_indicator_color, INDICATOR_COLOR);
        int selectColor = a.getColor(R.styleable.WheelView_select_color, SELECT_COLOR);
        int unselectColor = a.getColor(R.styleable.WheelView_unselect_color, UNSELECT_COLOR);
        float indicatorRatio = a.getFloat(R.styleable.WheelView_indicator_ratio, INDICATOR_RATIO_DEFAULT);
        // 回收TypedArray
        a.recycle();
        if (DEBUG) Log.d(TAG, "displayNumber="+displayNumber+", currentItem="+currentItem+", dataArrayId="+dataArrayId);

        setItemHeight(itemHeight);
        setSelectColor(selectColor);
        setUnselectColor(unselectColor);
        setIndiacatorColor(indicatorColor);
        setIndiacatorRatio(indicatorRatio);
        setDisplayNumber(displayNumber);
        setCurrentItem(currentItem);

        // TextViews Param
        mTextViewLayoutParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, mItemHeight);
        mTextViewLayoutParam.gravity = Gravity.CENTER;

        setDataArray(dataArrayId);

        addView(mContainer);
    }

    public void setDataArray(int arrayResId) {
        if (arrayResId==-1) {
            return ;
        }

        // 清除mDataList
        mDataList.clear();

        TypedArray array = getContext().getResources().obtainTypedArray(arrayResId);
        for (int i=0; i<array.length(); i++) {
            mDataList.add(array.getString(i));
        }
        array.recycle();

        setDataArray();
        if (DEBUG) Log.d(TAG, "setData by Res: mDataList.size()="+mDataList.size());
    }

    /*
     * 设置WheelView的数据
     */
    public void setDataArray(int start, int end) {
        List<String> list = new ArrayList<String>();
        for (int i=start; i<=end; i++) {
            list.add(String.valueOf(i));
        }
        setDataArray(list);
    }

    /*
     * 设置WheelView的数据
     */
    public void setDataArray(String[] strs) {
        List<String> list = Arrays.asList(strs);
        setDataArray(list);
    }
    /*
     * 设置WheelView的数据
     */
    public void setDataArray(List<String> list) {
        mDataList.clear();
        mDataList.addAll(list);
        setDataArray();
    }

    private void setDataArray() {
        // 清空mContainer
        mContainer.removeAllViews();

        for (String text:mDataList) {
            mContainer.addView(createTextView(text), mTextViewLayoutParam);
        }

        // 添加 顶部和底部的偏移
        for (int i=0; i<mOffset; i++) {
            mContainer.addView(createTextView(""), 0, mTextViewLayoutParam);
            mContainer.addView(createTextView(""), mTextViewLayoutParam);
        }

        // (重新)初始化颜色
        initTextViews();
        if (getCurrentItem() >= mDataList.size()) {
            // 防止数据改变之后，选中项发生了变化
            setCurrentItem(mDataList.size()-1);
        } else {
            removeCallbacks(mScrollTask);
            post(mScrollTask);
        }
    }

    /**
     * 创建TextView
     */
    private TextView createTextView(CharSequence text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(mSelectSize);
        tv.setGravity(Gravity.CENTER);

        return tv;
    }

    /**
     * 更新TextView
     */
    private void initTextViews() {
        int start = mOffset;
        int end = start + mDataList.size();
        for (int i=start; i<end; i++) {
            TextView tv = (TextView) mContainer.getChildAt(i);
            tv.setTextSize(i==mIndex ? mSelectSize : mUnselectSize);
            tv.setTextColor(i==mIndex ? mSelectColor : mUnselectColor);
        }
        if (DEBUG) Log.d(TAG, "init TextViews: mIndex: "+mIndex);
    }

    /**
     * 更新TextView
     */
    private void refreshTextViews() {
        int position = getIndexByScroll();

        if (position != mScrollPosition) {
            int start = mOffset;
            int end = mContainer.getChildCount() - mOffset;
            for (int i=start; i<end; i++) {
                TextView tv = (TextView) mContainer.getChildAt(i);
                tv.setTextSize(i==position ? mSelectSize : mUnselectSize);
                tv.setTextColor(i==position ? mSelectColor : mUnselectColor);
            }
            if (DEBUG) Log.d(TAG, "refresh: position: "+mScrollPosition+" -> "+position);
            mScrollPosition = position;
        }
    }

    /**
     * 计算WheelView中每一项的高度。
     *
     * 这里使用了一个比较Tricky的实现，在TextView的onMeasure()调用之前，
     * 先通过measure(0, 0)获取它的高度。
     */
    private int calculateItemHeight() {
        TextView tv = new TextView(getContext());
        tv.setText("M");
        tv.setTextSize(mSelectSize);
        tv.measure(0, 0);
        return tv.getMeasuredHeight();
    }

    /*
     * 设置每一项的高度
     */
    public void setItemHeight(int height) {
        if (height>mItemHeight && height<3*mItemHeight) {
            mItemHeight = height;
        }
    }

    /*
     * 设置选中字体的颜色
     */
    public void setSelectColor(int color) {
        mSelectColor = color;
    }

    /*
     * 设置未选中字体的颜色
     */
    public void setUnselectColor(int color) {
        mUnselectColor = color;
    }

    /*
     * 设置WheelView指示线的颜色
     */
    public void setIndiacatorColor(int color) {
        if (mLinePaint!=null) {
            mLinePaint.setColor(color);
        }
    }

    /*
     * 设置WheelView指示线的占宽比
     */
    public void setIndiacatorRatio(float ratio) {
        mIndicatorRatio = ratio;
    }

    /*
     * 设置WheelView中显示TextView的个数
     */
    public void setDisplayNumber(int num) {
        if (num%2 != 1 || num<DISPLAY_NUMBER_DEFAULT || num>DISPLAY_NUMBER_MAX) {
            mDisplayNum = DISPLAY_NUMBER_DEFAULT;
        } else {
            mDisplayNum = num;
        }
        mOffset = mDisplayNum/2;
        mIndex = mOffset;
    }

    /*
     * 设置WheelView的当前选项
     */
    public void setCurrentItem(int index) {
        mIndex = index + mOffset;
        removeCallbacks(mScrollTask);
        post(mScrollTask);
        if (DEBUG) Log.d(TAG, "setCurrentItem to "+mIndex);
    }

    /*
     * 获取WheelView的当前选项
     */
    public int getCurrentItem() {
        return mIndex - mOffset;
    }

    /**
     * 根据当前的偏移来获取被选中项的索引
     */
    private int getIndexByScroll() {
        int scrollY = getScrollY();
        int itemHeight = mItemHeight;
        int position = mOffset + scrollY/itemHeight;
        int remainder = scrollY % itemHeight;

        if (remainder > itemHeight/2) {
            position++;
        }

        return position;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 如果初始位置不是第一项：发送消息到消息队列中去更改位置。
        if (mIndex != mOffset) {
            post(mScrollTask);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int count = mContainer.getChildCount();
        if (count >= mDisplayNum) {
            View child = mContainer.getChildAt(0);
            // 每项的高度
            int itemHeight = mItemHeight;
            // 可见的高度
            int displayHeight = mDisplayNum * itemHeight;
            // WheelView的高度
            int wheelHeight = getPaddingTop() + displayHeight + getPaddingBottom();

            // 设置 WheelView的大小
            int totalWidthMeasureSpec  = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
            int totalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(wheelHeight, MeasureSpec.EXACTLY);
            setMeasuredDimension(totalWidthMeasureSpec, totalHeightMeasureSpec);

            // if (DEBUG) Log.d(TAG, "onMeasure, Container:"+mContainer.getMeasuredWidth()+"x"+mContainer.getMeasuredHeight()
            //         +", WheelView:"+getMeasuredWidth()+"x"+getMeasuredHeight()
            //         +", Item:"+child.getMeasuredWidth()+"x"+child.getMeasuredHeight()
            //         +", mOffset:"+mOffset);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {

        Drawable drawable = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                int itemHeight = mItemHeight;
                int wheelWidth = getWidth();
                int indicatorWidth = (int)((float)wheelWidth * mIndicatorRatio);
                int startX = (wheelWidth - indicatorWidth) / 2;
                int stopX = startX + indicatorWidth;
                int y1   = mOffset * itemHeight;
                int y2 = y1 + itemHeight;

                canvas.drawLine(startX, y1, stopX, y1, mLinePaint);
                canvas.drawLine(startX, y2, stopX, y2, mLinePaint);
                // if (DEBUG) Log.d(TAG, "line1: ("+startX+","+y1+") , ("+stopX+","+y1+")"
                //     +", line2: ("+startX+","+y1+") , ("+stopX+","+y1+")");
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter cf) {
            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };

        super.setBackgroundDrawable(drawable);
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // Log.d(TAG, "onScrollChanged, scrollY:"+getScrollY());

        refreshTextViews();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            startScrollCheckTask();
        }

        return super.onTouchEvent(ev);
    }

    /*
     * 启动ScrollView的Check Task，检查ScrollView是否停止滚动。
     */
    private void startScrollCheckTask() {
        mInitialPosition = getScrollY();
        postDelayed(mScrollCheckTask, CHECK_FREQUENCE);
        if (DEBUG) Log.d(TAG, "startScrollCheckTask, mInitialPosition:"+mInitialPosition);
    }

    /**
     * ScrollView的Check Task，检查ScrollView是否停止滚动。
     *
     * (01) 当ScrollView停止滚动时，则将TextView平移到指定位置；
     * (02) 否则，隔CHECK_FREQUENCE之后，再次启动Check Task
     */
    private Runnable mScrollCheckTask = new Runnable() {

        @Override
        public void run() {
            int currPosition = getScrollY();

            // 当scrollY停止变化时，表示ScrollView已经停止滚动。
            if(mInitialPosition-currPosition == 0){
                if (DEBUG) Log.d(TAG, "check scroll success!");

                int itemHeight = mItemHeight;
                // 滚动 对应的索引
                int scrollIndex = currPosition/itemHeight;
                // 滚动 对应的偏移
                int scrollRemainder = currPosition % itemHeight;

                // 当"可见的TextView不需要偏移时"，则不进行任何操作。
                if (scrollRemainder == 0) {
                    mIndex = mOffset + scrollIndex;
                    onWheelChangeCallback();
                    return ;
                } else if (scrollRemainder > itemHeight/2) {
                    scrollIndex++;
                }
                if (DEBUG) Log.d(TAG, "smooth scroll "+currPosition+" -> "+scrollIndex*itemHeight);
                mIndex = mOffset + scrollIndex;
                WheelView.this.post(mScrollTask);
            }else{
                mInitialPosition = getScrollY();
                WheelView.this.postDelayed(mScrollCheckTask, CHECK_FREQUENCE);
                if (DEBUG) Log.d(TAG, "check scroll again, mInitialPosition="+mInitialPosition);
            }
        }
    };

    /**
     * WheelView变化的回调
     */
    private void onWheelChangeCallback() {
        if (mWheelChangeListener!= null) {
            if (mDataList.size() > 0) {
                int index = getCurrentItem();
                String value = mDataList.get(index);

                if (DEBUG) Log.d(TAG, "onWheelChangeCallback: index="+index+", value="+value);
                mWheelChangeListener.onSelected(index, value);
            }
        }
    }

    /**
     * 设置WheelView选择项发生变化的监听接口
     */
    public void setOnWheelChangeListener(OnWheelChangeListener listener) {
        mWheelChangeListener = listener;
    }

    private Runnable mScrollTask = new Runnable() {
        @Override
        public void run() {
            smoothScrollTo(0, (mIndex-mOffset)*mItemHeight);
            onWheelChangeCallback();
        }
    }; 
}
