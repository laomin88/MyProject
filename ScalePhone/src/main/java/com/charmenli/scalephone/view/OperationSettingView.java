package com.charmenli.scalephone.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.charmenli.scalephone.R;

/**
 * TODO: document your custom view class.
 */
public class OperationSettingView extends View {
    private int mScaleWindowBorderColor = Color.GRAY; // TODO: use a default from R.color...
    private Drawable mBackgroundDrawable;
    private int mScaleWidth = 75;
    private int mScaleHeight = 75;
    private int mScaleOffsetX = 1;
    private int mScaleOffsetY = 1;
    private Drawable mCoverDrawable;
    private int mCoverAlpha = 200;
    private Paint mPaint = new Paint();
    private Rect mOutSideRect;
    private OnScaleWindowMoveListener mScaleWindowMoveListener;


    public OperationSettingView(Context context) {
        super(context);
        init(null, 0);
    }

    public OperationSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OperationSettingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.OperationSettingView, defStyle, 0);

        mScaleWindowBorderColor = a.getColor(
                R.styleable.OperationSettingView_scaleWindowBorderColor,
                mScaleWindowBorderColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.

        if (a.hasValue(R.styleable.OperationSettingView_backgroundDrawable)) {
            mBackgroundDrawable = a.getDrawable(
                    R.styleable.OperationSettingView_backgroundDrawable);
            mBackgroundDrawable.setCallback(this);
        }

        initScaleWindow(a);

        a.recycle();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mScaleWindowBorderColor);
        setOnTouchListener(myTouchListener);
    }

    private void initScaleWindow(TypedArray a) {
        if (a.hasValue(R.styleable.OperationSettingView_coverDrawable)) {
            mCoverDrawable = a.getDrawable(
                    R.styleable.OperationSettingView_coverDrawable);
            mCoverDrawable.setCallback(this);

            mCoverAlpha = a.getInt(R.styleable.OperationSettingView_coverAlpha, mCoverAlpha);
            mCoverDrawable.setAlpha(mCoverAlpha);
        }

        mScaleWidth = a.getInt(R.styleable.OperationSettingView_scaleWidth, mScaleWidth);
        mScaleHeight = a.getInt(R.styleable.OperationSettingView_scaleHeight, mScaleHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the example drawable on top of the text.
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mBackgroundDrawable.draw(canvas);
        }

        if (mOutSideRect == null) {
            mOutSideRect = new Rect(paddingLeft, paddingTop, paddingLeft + contentWidth, paddingTop + contentHeight);
        }
        drawRect(canvas, mOutSideRect);

        // Draw the cover on top of background
        if (mCoverDrawable != null) {
            mCoverDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mCoverDrawable.draw(canvas);
        }

        // Draw the scale window on top of the cover
        if (mBackgroundDrawable != null) {
            int left = paddingLeft + mScaleOffsetX;
            int top = paddingTop + mScaleOffsetY;
            int scaleWidth = getScaleWindowWidth(contentWidth);
            int scaleHeight = getScaleWindowHeight(contentHeight);
            int right = left + scaleWidth;
            int bottom = top + scaleHeight;
            Bitmap scaleWindowBitmap = Bitmap.createScaledBitmap(drawableToBitmap(mBackgroundDrawable),
                    scaleWidth, scaleHeight, true);
            canvas.drawBitmap(scaleWindowBitmap, left, top, null);

            Rect r = new Rect(left-1, top-1, right+1, bottom+1);
            drawRect(canvas, r);
        }
    }

    private void drawRect(Canvas canvas, Rect r) {
        canvas.drawRect(r, mPaint);
    }

    /**
     * Gets the example color attribute value.
     * @return The example color attribute value.
     */
    public int getColor() {
        return mScaleWindowBorderColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     * @param color The example color attribute value to use.
     */
    public void setColor(int color) {
        mScaleWindowBorderColor = color;
    }

    /**
     * Gets the example drawable attribute value.
     * @return The example drawable attribute value.
     */
    public Drawable getBackground() {
        return mBackgroundDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     * @param backgroundDrawable The example drawable attribute value to use.
     */
    public void setBackground(Drawable backgroundDrawable) {
        mBackgroundDrawable = backgroundDrawable;
    }

    private int getScaleWindowWidth(int width) {
        double scale = mScaleWidth / 100.0;
        if (scale >= 1) {
            return width;
        }
        return (int)(width*scale);
    }

    private int getScaleWindowHeight(int height) {
        double scale = mScaleHeight / 100.0;
        if (scale >= 1) {
            return height;
        }
        return (int)(height*scale);
    }

    private static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setScaleWidth(int scaleWidth) {
        this.mScaleWidth = scaleWidth;
    }

    public void setScaleHeight(int scaleHeight) {
        this.mScaleHeight = scaleHeight;
    }

    public int getScaleWindowBorderColor() {
        return mScaleWindowBorderColor;
    }

    public void setScaleWindowBorderColor(int mScaleWindowBorderColor) {
        this.mScaleWindowBorderColor = mScaleWindowBorderColor;
    }

    public Drawable getBackgroundDrawable() {
        return mBackgroundDrawable;
    }

    public void setBackgroundDrawable(Drawable mBackgroundDrawable) {
        this.mBackgroundDrawable = mBackgroundDrawable;
    }

    public int getScaleOffsetX() {
        return mScaleOffsetX;
    }

    public void setScaleOffsetX(int mScaleOffsetX) {
        this.mScaleOffsetX = mScaleOffsetX;
    }

    public int getScaleOffsetY() {
        return mScaleOffsetY;
    }

    public void setScaleOffsetY(int mScaleOffsetY) {
        this.mScaleOffsetY = mScaleOffsetY;
    }

    public int getCoverAlpha() {
        return mCoverAlpha;
    }

    public void setCoverAlpha(int mCoverAlpha) {
        this.mCoverAlpha = mCoverAlpha;
    }

    public Drawable getCoverDrawable() {
        return mCoverDrawable;
    }

    public void setCoverDrawable(Drawable mCoverDrawable) {
        this.mCoverDrawable = mCoverDrawable;
    }

    public OnScaleWindowMoveListener getScaleWindowMoveListener() {
        return mScaleWindowMoveListener;
    }

    public void setScaleWindowMoveListener(OnScaleWindowMoveListener mScaleWindowMoveListener) {
        this.mScaleWindowMoveListener = mScaleWindowMoveListener;
    }

    private boolean mDown = false;
    private float mDownX = 0;
    private float mDownY = 0;
    private OnTouchListener myTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()&MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mDown = true;
                    mDownX = event.getX() - mScaleOffsetX;
                    mDownY = event.getY() - mScaleOffsetY;
                    if (mScaleWindowMoveListener != null) {
                        mScaleWindowMoveListener.onTouchDown(v, event);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mDown) {
                        mScaleOffsetX = (int)(event.getX() - mDownX);
                        mScaleOffsetY = (int)(event.getY() - mDownY);
                    }
                    if (mScaleWindowMoveListener != null) {
                        mScaleWindowMoveListener.onTouchMove(v, event);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mDown = false;
                    mDownX = 0;
                    mDownY = 0;
                    if (mScaleWindowMoveListener != null) {
                        mScaleWindowMoveListener.onTouchUp(v, event);
                    }
                    break;
            }
            invalidate();
            return true;
        }
    };

    public static interface OnScaleWindowMoveListener {
        void onTouchDown(View v, MotionEvent event);
        void onTouchMove(View v, MotionEvent event);
        void onTouchUp(View v, MotionEvent event);
    }

}
