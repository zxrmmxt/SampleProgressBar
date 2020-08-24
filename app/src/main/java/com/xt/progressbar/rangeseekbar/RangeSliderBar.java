package com.xt.progressbar.rangeseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xt.sampleprogressbar.R;


/**
 * Created by xuti on 2018/8/9.
 */
public class RangeSliderBar extends View {
    private static final String  TAG                        = "RangeSliderBar";
    private              Context context;
    /**
     * 画笔的默认的宽度
     */
    private static final int     DEFAULT_PAINT_STROKE_WIDTH = 8;
    /**
     * seekBar的默认二级背景
     */
    private static final int     DEFAULT_FILLED_COLOR       = Color.parseColor("#00b0ff");
    /**
     * seekBar的默认一级背景
     */
    private static final int     DEFAULT_EMPTY_COLOR        = Color.parseColor("#333333");
    /**
     * rangebar粗细
     */
    private static final float   DEFAULT_BAR_HEIGHT_PERCENT = 0.10f;

    /**
     * 设置自定义seekBar的长度
     * 这个长度要与传进来数组的长度一致
     */
    private static final int DEFAULT_RANGE_COUNT = 5;

    /**
     * rangebar高度
     */
    private static final int DEFAULT_HEIGHT_IN_DP = 90;

    /**
     * seekBar的画笔
     */
    protected Paint    paint;
    /**
     * 分段显示的画笔
     */
    protected Paint    mPaint;
    /**
     * 当前的索引位置
     */
    private   int      currentIndex;
    /**
     * 当前的X坐标
     */
    private   float    currentSlidingX;
    /**
     * 当前的Y坐标
     */
    private   float    currentSlidingY;
    /**
     * 具体分段显示的数值
     */
    private   String[] circlePositions;

    /**
     * 分段显示的距离数组
     */
    private float[] circlePositions2 = new float[DEFAULT_RANGE_COUNT];
    /**
     * 填充颜色
     */
    private int     filledColor      = DEFAULT_FILLED_COLOR;
    /**
     * 空白颜色
     */
    private int     emptyColor       = DEFAULT_EMPTY_COLOR;

//    private float barHeightPercent = DEFAULT_BAR_HEIGHT_PERCENT;
    /**
     * 分段显示的数量
     */
    private int rangeCount = DEFAULT_RANGE_COUNT;

    private int barHeight, locationY;
    private float downX;
    private float downY;

    /**
     * seekBar滑块的图片
     */
    private Bitmap bitmap_point;

    private int layoutHeight;
//    private boolean mPopupStyle;//是否显示pop

    private PopupWindow mPopup;
    View     popView;
    TextView tv;
    int      indexNum; //位置数值
    private int[] mPosition;

    public RangeSliderBar(Context context) {
        this(context, null);
        this.context = context;
        initPop();
    }

    public RangeSliderBar(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
        this.context = context;
        initPop();
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public void setShowPopText(String str) {
        tv.setText(str);
    }

    void initPop() {
        mPosition = new int[2];
//        popView = LayoutInflater.from(context).inflate(R.layout.seekbar_pop, null);
//        mPopup = new PopupWindow(popView, popView.getWidth(), popView.getHeight(), true);
//        tv = (TextView) popView.findViewById(R.id.tv_showtxt);
    }


    public RangeSliderBar(Context context, AttributeSet attrs, int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //自定义属性
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable
                    .RangeSliderView);
            TypedArray sa = context.obtainStyledAttributes(attrs, new
                    int[]{android.R.attr.layout_height});
            try {
                layoutHeight = sa.getLayoutDimension(
                        0, 0);
                rangeCount = a.getInt(
                        R.styleable.RangeSliderView_rangeCount,
                        DEFAULT_RANGE_COUNT);
                filledColor = a.getColor(
                        R.styleable.RangeSliderView_filledColor,
                        DEFAULT_FILLED_COLOR);
                emptyColor = a.getColor(
                        R.styleable.RangeSliderView_emptyColor,
                        DEFAULT_EMPTY_COLOR);
                /*barHeight = dpToPx(getContext(), a.getDimension(
                        R.styleable.RangeSliderView_barHeight,
                        dpToPx(getContext(), 4)));*/
                barHeight = (int) a.getDimension(
                        R.styleable.RangeSliderView_barHeight,
                        4);
            } finally {
                a.recycle();
                sa.recycle();
            }
        }
//        setBarHeightPercent(barHeightPercent);
        setRangeCount(rangeCount);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(DEFAULT_PAINT_STROKE_WIDTH);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(dpToPx(getContext(), 8));
//        mPaint.setAntiAlias(true);
        //自定义控件UI的监听器
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);

                // Update radius after we got new height
                updateRadius(getHeight());
                // Compute drawing position again
                preComputeDrawingPosition();
                // Ready to draw now
                return true;
            }
        });
        //设置默认选中第二个位置
        currentIndex = 0;
//        setShowPopText(circlePositions[currentIndex] + "");
    }


    private void updateRadius(int height) {
//        barHeight = (int) (height * barHeightPercent);
        locationY = barHeight + DEFAULT_HEIGHT_IN_DP / 2; //Y位置
    }

    /**
     * 设置分段显示的数据
     */
    public void setTexts(String[] texts) {
        this.circlePositions = texts;
    }

    /**
     * 设置分段显示的长度，必须与数据的大小一致!
     *
     * @param rangeCount
     */
    public void setRangeCount(int rangeCount) {
        if (rangeCount < 2) {
            throw new IllegalArgumentException("rangeCount must be >= 2");
        }
        this.rangeCount = rangeCount;
    }


    /*public void setBarHeightPercent(float percent) {
        if (percent <= 0.0 || percent > 1.0) {
            throw new IllegalArgumentException("Bar height percent must be in" +
                    " (0, 1]");
        }
        this.barHeightPercent = percent;
    }*/


    /**
     * Perform all the calculation before drawing, should only run once
     * 计算各分段显示的位置,这里采取均匀分开！
     */
    private void preComputeDrawingPosition() {
        int w = getWidthWithPadding();
        int h = getHeightWithPadding();

        initPop();
        /** Space between each circle */
        int spacing = w / rangeCount;
        /** Center vertical */
        int y = getPaddingTop() + h / 2;
        currentSlidingY = y;
        int x = getPaddingLeft() + (spacing / 2);
        /** Store the position of each circle index */
        for (int i = 0; i < circlePositions.length; ++i) {
            circlePositions2[i] = x;
            Log.e("--------->", "circlePositions[i]==" + circlePositions[i]);
            if (i == currentIndex) {
                currentSlidingX = x;
            }
            //这里采用均匀距离，若是想根据值来显示距离，这里需要自己修改！
            x += spacing;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    /**
     * Measures height according to the passed measure spec
     *
     * @param measureSpec int measure spec to use
     * @return int pixel size
     */
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            final int height;
            if (layoutHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
                height = dpToPx(getContext(), DEFAULT_HEIGHT_IN_DP);
            } else if (layoutHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
                height = getMeasuredHeight();
            } else {
                height = layoutHeight;
            }
            result = height + getPaddingTop() + getPaddingBottom() + (2 *
                    DEFAULT_PAINT_STROKE_WIDTH);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Measures width according to the passed measure spec
     *
     * @param measureSpec int measure spec to use
     * @return int pixel size
     */
    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = specSize + getPaddingLeft() + getPaddingRight() + (2 *
                    DEFAULT_PAINT_STROKE_WIDTH);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    //校准索引
    private void updateCurrentIndex() {
        float min = Float.MAX_VALUE;
        int   j   = 0;
        /** Find the closest to x */
        for (int i = 0; i < rangeCount; ++i) {
            float dx = Math.abs(currentSlidingX - circlePositions2[i]);
            if (dx < min) {
                min = dx;
                j = i;
            }
        }
        setCurrentIndex(j);
    }

    public void setCurrentIndex(int index) {
        //计算出索引
        currentIndex = index;
        /** Correct position */
        currentSlidingX = circlePositions2[index];
        invalidate();
        if (onSelectRangeBarListener != null) {
//                    int range = circlePositions[currentIndex];
//                    setShowPopText(range + "");
            onSelectRangeBarListener.OnSelectRange(currentIndex);
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float     y      = event.getY();
        float     x      = event.getX();
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                this.getLocationOnScreen(mPosition);
//                mPopup.showAsDropDown(this, (int) downX, -170 - bitmap_point.getHeight());
                break;

            case MotionEvent.ACTION_MOVE:
                if (x >= circlePositions2[0] && x <=
                        circlePositions2[rangeCount - 1]) {
                    currentSlidingX = x;
                    currentSlidingY = y;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                currentSlidingX = x;
                currentSlidingY = y;
//                mPopup.dismiss();
                updateCurrentIndex();
                break;
        }
        return true;
    }


    private void drawDefaultCircle(Canvas canvas) {
        int textSize = dpToPx(getContext(), 8);
        mPaint.setTextSize(textSize);
        int h = getHeightWithPadding();
//        int y = getPaddingTop() + (h >> 1);
        for (int i = 0; i < circlePositions.length; ++i) {
            //每个对应数字下画竖线
            /**
             * 画线参数:startX,startY,endX,endY
             * 画文字参数:文字，起始坐标X，起始坐标Y
             */
//            canvas.drawLine(circlePositions2[i], y + barHeight + 5, circlePositions2[i], y + barHeight + 20, mPaint);
            String circlePosition = circlePositions[i];
            drawText(canvas, textSize, circlePosition, i);
        }
    }

    private void drawText(Canvas canvas, int textSize, String text, int index) {
        int xOffset = text.length() * textSize / 2;
        if (index == 0) {
            xOffset = 0;
        }
        if (index == rangeCount - 1) {
            xOffset = xOffset * 2;
        }
        if (index == currentIndex) {
            mPaint.setColor(filledColor);
        } else {
            mPaint.setColor(Color.parseColor("#807e81"));
        }
        canvas.drawText(text, circlePositions2[index] - xOffset, getHeightWithPadding() / 2 + barHeight / 2 + textSize + dpToPx(getContext(), 5), mPaint);
    }

    private void drawSlidRangeCircle(Canvas canvas, int x0, int spc) {
        paint.setColor(filledColor);
        int h = getHeightWithPadding();
        int y = getPaddingTop() + (h >> 1);
        for (int i = 0; i < circlePositions.length; ++i) {
            canvas.drawText(circlePositions[i] + "", circlePositions2[i] - 10, y + barHeight + 50, mPaint);
        }
    }


    /**
     * 画rangebar
     *
     * @param canvas
     * @param from
     * @param to
     * @param color
     */
    private void drawBar(Canvas canvas, int from, int to, int color) {
        paint.setColor(color);
        int half = (barHeight >> 1);
        int y    = getPaddingTop() + getHeightWithPadding() / 2; //设置bar位置
//        int y = getPaddingTop() + barHeight; //设置bar位置

        /**
         * 画长方形
         */
//        canvas.drawRect(from, y - half, to, y + half, paint);
        /**
         * 画圆角矩形
         */
        RectF oval3 = new RectF(from, y - half, to, y + half);
        canvas.drawRoundRect(oval3, 5, 5, paint);//画圆角矩形
        /**
         * 画线
         */
//        canvas.drawLine(from, y, to, y, paint);
    }

    private int getViewWidth(View v) {
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
        return v.getMeasuredWidth();
    }

    private int getViewHeight(View v) {
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
        return v.getMeasuredHeight();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w       = getWidthWithPadding();
        int spacing = w / rangeCount;
        int border  = (spacing >> 1);
        int x0      = getPaddingLeft() + border;
        if (mPopup != null) {
            try {
                this.getLocationOnScreen(mPosition);
                mPopup.update((int) downX, -170, getViewWidth(popView)
                        , getViewHeight(popView));
            } catch (Exception e) {
            }
        }

//        bitmap_point = BitmapFactory.decodeResource(getResources(), AppMgr.iDraw("dot_chart"));
//        bitmap_point = BitmapFactory.decodeResource(getResources(), R.mipmap.seekbar_yelloe);
        bitmap_point = BitmapFactory.decodeResource(getResources(), R.mipmap.yuandian);
        /** Draw empty bar 画一级背景*/
        drawBar(canvas, (int) circlePositions2[0], (int)
                circlePositions2[rangeCount - 1], emptyColor);
        /** Draw filled bar 画二级背景*/
        if (currentSlidingX != circlePositions2[0]) {
            drawBar(canvas, (int) circlePositions2[0], (int) currentSlidingX, filledColor);
        }

        int bitmap_pointHeight = bitmap_point.getHeight();
        Log.e(TAG, bitmap_pointHeight + ""); //56
        //seekBar滑块的位置，这里根据自己的需要进行调整
        //画移动背景图

        canvas.drawBitmap(bitmap_point, (int) currentSlidingX - bitmap_point.getWidth() / 2, (getHeightWithPadding() / 2 - bitmap_point.getHeight()) - dpToPx(getContext(), 5), null);
        /** Draw the selected range circle */
        paint.setColor(filledColor);
        drawDefaultCircle(canvas);
//        drawSlidRangeCircle(canvas, (int) currentSlidingX, spacing);
    }


    public int getHeightWithPadding() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    public int getWidthWithPadding() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }


    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss         = new SavedState(superState);
        ss.saveIndex = this.currentIndex;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.currentIndex = ss.saveIndex;
    }

    /**
     * 保存状态的类，横竖屏切换的时候用来保存切换之前的状态
     */
    static class SavedState extends BaseSavedState {
        int saveIndex;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.saveIndex = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.saveIndex);
        }

        //required field that makes Parcelables from a Parcel
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    /**
     * Helper method to convert dp to pixel
     *
     * @param context
     * @param dp
     * @return
     */
    static int dpToPx(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }


    OnSelectRangeBarListener onSelectRangeBarListener;

    /**
     * 自定义接口，提供接口让外部实现处理自己的逻辑
     */
    public interface OnSelectRangeBarListener {
        /**
         * 实现接口需要实现的方法，这里的参数rangeNum就是seekBar滑动停止后的位置所对应的数据
         */
        void OnSelectRange(int index);
    }

    public void setOnSelectRangeBarListener(OnSelectRangeBarListener
                                                    onSelectRangeBarListener) {
        this.onSelectRangeBarListener = onSelectRangeBarListener;
    }
}
