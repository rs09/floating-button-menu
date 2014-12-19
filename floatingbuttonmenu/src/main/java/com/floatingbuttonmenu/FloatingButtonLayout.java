package com.floatingbuttonmenu;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.floatingbuttonmenu.animation.FloatingButtonAnimationHandlerBase;

class FloatingButtonLayout extends ViewGroup {

    public static final float FROM_DEGREES_DEFAULT = 180.0f;
    public static final float TO_DEGREES_DEFAULT = 270.0f;
    public static final int DEFAULT_RADIUS = 300;

    private int mRadius = DEFAULT_RADIUS;
    private float mFromDegrees = FROM_DEGREES_DEFAULT;
    private float mToDegrees = TO_DEGREES_DEFAULT;

    private Point mFloatingButtonCenter;
    private Point mFloatingButtonTopCornerCoor;

    private int mFloatingButtonWidth;
    private int mFloatingButtonHeight;

    private boolean mExpanded;

    private FloatingButtonAnimationHandlerBase mAnimationHandler;

    public FloatingButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingButtonLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        mFloatingButtonCenter = new Point(0, 0);
        mFloatingButtonTopCornerCoor = new Point(0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(parentWidth, parentHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int centerX = mFloatingButtonCenter.x;
        final int centerY = mFloatingButtonCenter.y;

        final int radius = mExpanded ? mRadius : 0;

        final int childCount = getChildCount();

        final float degreesPerChild = (mToDegrees - mFromDegrees) / (childCount - 1);

        float degrees = mFromDegrees;

        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);

            Rect childFrame = FloatingButtonMenu.computeChildFrame(centerX, centerY, radius, degrees, v.getMeasuredWidth(), v.getMeasuredHeight());
            getChildAt(i).layout(childFrame.left, childFrame.top, childFrame.right, childFrame.bottom);

            degrees += degreesPerChild;
        }
    }

    public void toggleState(boolean animate) {
        setState(!mExpanded, animate);
    }

    public void setState(boolean expanded, boolean animate) {
        if (mAnimationHandler.isAnimating() || mExpanded == expanded) {
            return;
        }

        mExpanded = expanded;

        if (animate) {
            mAnimationHandler.animateMenu();
        }

        requestLayout();
    }

    public void setArc(float fromDegrees, float toDegrees) {
        if (mFromDegrees == fromDegrees && mToDegrees == toDegrees) {
            return;
        }

        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;

        requestLayout();
    }

    public void setRadius(int radius) {
        if (mRadius == radius) {
            return;
        }

        mRadius = radius;

        requestLayout();
    }

    public void setAnimationHandler(FloatingButtonAnimationHandlerBase animationHandler) {
        mAnimationHandler = animationHandler;
    }

    public float getToDegrees() {
        return mToDegrees;
    }

    public float getFromDegrees() {
        return mFromDegrees;
    }

    public Point getFloatingButtonTopCornerCoor() {
        return mFloatingButtonTopCornerCoor;
    }

    public Point getFloatingButtonCenter() {
        return mFloatingButtonCenter;
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setFloatingButtonParams(Point coor, int width, int height) {
        mFloatingButtonTopCornerCoor = coor;

        mFloatingButtonWidth = width;
        mFloatingButtonHeight = height;

        mFloatingButtonCenter = new Point(coor.x + width / 2, coor.y + height / 2);

        requestLayout();
    }

    /**
     * Returns a boolean stating whether the x, y coordinates passed lie withing the FloatingButtonLayout area.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isPointOutsideFloatingButtonLayout(int x, int y) {
        Rect floatingButtonLayoutArea = getFloatingButtonLayoutTouchArea();
        return !floatingButtonLayoutArea.contains(x, y);
    }

    /**
     * Returns the approximate area of the Floating Button layout taking into consideration if it's opened or not. We also
     * add an extra padding to this value.
     *
     * @return
     */
    private Rect getFloatingButtonLayoutTouchArea() {
        int padding = getResources().getDimensionPixelOffset(R.dimen.floating_button_layout_additional_padding);
        int xCoor = mFloatingButtonTopCornerCoor.x;
        int yCoor = mFloatingButtonTopCornerCoor.y;

        int left = xCoor - padding;
        int top = yCoor - padding;
        if (mExpanded && getChildCount() > 0) {
            View childView = getChildAt(0);

            int childWidth = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();

            left -= (mRadius + childWidth - mFloatingButtonWidth / 2);
            top -= (mRadius + childHeight - mFloatingButtonHeight / 2);
        }
        int right = xCoor + mFloatingButtonWidth + padding;
        int bottom = yCoor + mFloatingButtonHeight + padding;

        return new Rect(left, top, right, bottom);
    }

}
