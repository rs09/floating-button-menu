package com.floatingbuttonmenu.animation;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

import com.floatingbuttonmenu.FloatingButtonMenu;

/**
 * Base class for making animation handlers for the FloatingButtonLayout. Any class extending this class should also implement a Builder
 * which extends this class's Builder class.
 */
public abstract class FloatingButtonAnimationHandlerBase {

    private static final long START_OFFSET_CHILD_DEFAULT = 80;
    private static final long DURATION_DEFAULT = 500;
    private static final long BG_DURATION = 200;

    protected FloatingButtonMenu mFloatingButtonMenu;
    protected Context mContext;
    protected long startOffsetBetweenEachChild;
    protected long duration;
    protected Interpolator openInterpolator;
    protected Interpolator closeInterpolator;

    private boolean mIsAnimating;

    protected FloatingButtonAnimationHandlerBase(FloatingButtonMenu floatingButtonMenu) {
        this.mFloatingButtonMenu = floatingButtonMenu;
        this.mContext = floatingButtonMenu.getContext();
    }

    public void animateMenu() {
        mIsAnimating = true;

        final int count = mFloatingButtonMenu.getMenuChildCount();
        final float toDegrees = mFloatingButtonMenu.getToDegrees();
        final float fromDegrees = mFloatingButtonMenu.getFromDegrees();
        final boolean expanded = mFloatingButtonMenu.isExpanded();

        final float degreesPerChild = (toDegrees - fromDegrees) / (count - 1);

        float degrees = fromDegrees;

        for (int i = 0; i < count; i++) {
            final View childView = mFloatingButtonMenu.getMenuChildAt(i);

            int transformedIndex = getTransformedIndex(i, count, expanded);

            Animation animation = getAnimation(childView, transformedIndex, degrees, expanded);

            final boolean lastItem = transformedIndex == count - 1;
            final boolean firstItem = transformedIndex == 0;


            /*
            We want the background to be visible when the view is opening and we are about to animate
            the first item.
             */
            if (firstItem && expanded) {
                handleBackgroundView(expanded);
            }
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (expanded) {
                        childView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!expanded) {
                        childView.setVisibility(View.INVISIBLE);
                    }
                    if (lastItem) {
                        mIsAnimating = false;

                        FloatingButtonMenu.OnStateChangeListener menuStateListener = mFloatingButtonMenu.getOnStateChangeListener();
                        if (menuStateListener != null) {
                            menuStateListener.onMenuStateChanged(expanded);
                        }

                        /*
                        We want the background to be invisible when the view is closing and we have
                        finished animating everything
                        */
                        if (!expanded) {
                            handleBackgroundView(expanded);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            childView.setAnimation(animation);
            degrees += degreesPerChild;
        }
    }

    protected Animation getTranslateAnimation(View childView, float degrees, boolean expanded) {
        final Point coor = mFloatingButtonMenu.getFloatingButtonTopCornerCoor();
        final int xCoor = coor.x;
        final int yCoor = coor.y;

        final Point center = mFloatingButtonMenu.getFloatingButtonCenter();
        final int xCenter = center.x;
        final int yCenter = center.y;

        final int radius = expanded ? mFloatingButtonMenu.getRadius() : 0;

        Rect childFrame = FloatingButtonMenu.computeChildFrame(xCenter, yCenter, radius, degrees, childView.getMeasuredWidth(), childView.getMeasuredHeight());

        int fromX = expanded ? Math.abs(childFrame.left - xCoor) : (childView.getLeft() - childFrame.left);
        int fromY = expanded ? Math.abs(childFrame.top - yCoor) : (childView.getTop() - childFrame.top);

        return new TranslateAnimation(TranslateAnimation.ABSOLUTE, fromX, TranslateAnimation.ABSOLUTE, 0,
                TranslateAnimation.ABSOLUTE, fromY, TranslateAnimation.ABSOLUTE, 0);
    }

    protected Animation getAlphaAnimation(boolean expanded) {
        float fromAlpha = expanded ? 0.0f : 1.0f;
        float toAlpha = expanded ? 1.0f : 0.0f;

        return new AlphaAnimation(fromAlpha, toAlpha);
    }

    private Animation getBackgroundAlphaAnimation(boolean expanded) {
        Animation alphaAnimation = getAlphaAnimation(expanded);
        alphaAnimation.setDuration(BG_DURATION);
        return alphaAnimation;
    }

    private void handleBackgroundView(boolean expanded) {
        if (!mFloatingButtonMenu.shouldShowBackground()) {
            return;
        }
        View backgroundView = mFloatingButtonMenu.getBackgroundView();
        Animation animation = getBackgroundAlphaAnimation(expanded);

        backgroundView.startAnimation(animation);
        backgroundView.setVisibility(expanded ? View.VISIBLE : View.INVISIBLE);
    }

    protected Animation getRotateAnimation() {
        return new RotateAnimation(0.0f, 1440.0f, RotateAnimation.RELATIVE_TO_SELF, .5f, RotateAnimation.RELATIVE_TO_SELF, .5f);
    }

    protected long getChildStartOffset(int index) {
        return index * startOffsetBetweenEachChild;
    }

    /**
     * Returns the transformed index based on whether the layout is opening or closing. If opening the normal index is returned,
     * if closing we reverse the index.
     *
     * @param actualIndex
     * @param count
     * @param expanded
     * @return
     */
    private int getTransformedIndex(int actualIndex, int count, boolean expanded) {
        return expanded ? actualIndex : count - actualIndex - 1;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setStartOffsetBetweenEachChild(long startOffsetBetweenEachChild) {
        this.startOffsetBetweenEachChild = startOffsetBetweenEachChild;
    }

    public void setOpenInterpolator(Interpolator interpolator) {
        this.openInterpolator = interpolator;
    }

    public void setCloseInterpolator(Interpolator interpolator) {
        this.closeInterpolator = interpolator;
    }

    public boolean isAnimating() {
        return mIsAnimating;
    }

    protected abstract Animation getAnimation(View childView, int index, float degrees, boolean expanded);

    public abstract static class Builder<T extends Builder, U extends FloatingButtonAnimationHandlerBase> {
        protected FloatingButtonMenu mFloatingButtonMenu;
        protected long startOffsetBetweenEachChild;
        protected long duration;
        protected Interpolator openInterpolator;
        protected Interpolator closeInterpolator;

        protected Builder(FloatingButtonMenu floatingButtonMenu) {
            mFloatingButtonMenu = floatingButtonMenu;
            startOffsetBetweenEachChild = START_OFFSET_CHILD_DEFAULT;
            duration = DURATION_DEFAULT;
            openInterpolator = new OvershootInterpolator(1.5f);
            closeInterpolator = new DecelerateInterpolator(1.5f);
        }

        public T setStartOffsetBetweenEachChild(long startOffset) {
            this.startOffsetBetweenEachChild = startOffset;
            return (T) this;
        }

        public T setDuration(long duration) {
            this.duration = duration;
            return (T) this;
        }

        public T setOpenInterpolator(Interpolator interpolator) {
            this.openInterpolator = interpolator;
            return (T) this;
        }

        public T setCloseInterpolator(Interpolator interpolator) {
            this.closeInterpolator = interpolator;
            return (T) this;
        }

        protected void setCommonProperties(FloatingButtonAnimationHandlerBase animationHandlerBase) {
            animationHandlerBase.setDuration(duration);
            animationHandlerBase.setStartOffsetBetweenEachChild(startOffsetBetweenEachChild);
            animationHandlerBase.setOpenInterpolator(openInterpolator);
            animationHandlerBase.setCloseInterpolator(closeInterpolator);
        }

        public U build() {
            FloatingButtonAnimationHandlerBase animationHandler = makeAndSetSpecialProperties();
            setCommonProperties(animationHandler);
            return (U) animationHandler;
        }

        /**
         * This method is used to instantiate the AnimationHandler object and add the special properties
         * that that AnimationHandler might be having.
         *
         * @return
         */
        protected abstract FloatingButtonAnimationHandlerBase makeAndSetSpecialProperties();
    }
}
