package com.floatingbuttonmenu.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

import com.floatingbuttonmenu.FloatingButtonMenu;

public class TranslateAlphaFloatingButtonAnimationHandler extends FloatingButtonAnimationHandlerBase {

    protected TranslateAlphaFloatingButtonAnimationHandler(FloatingButtonMenu floatingButtonMenu) {
        super(floatingButtonMenu);
    }

    @Override
    protected Animation getAnimation(View childView, int index, float degrees, boolean expanded) {
        AnimationSet animationSet = new AnimationSet(true);

        Animation translateAnimation = getTranslateAnimation(childView, degrees, expanded);
        Animation alphaAnimation = getAlphaAnimation(expanded);

        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(duration);
        animationSet.setStartOffset(getChildStartOffset(index));

        animationSet.setInterpolator(expanded ? openInterpolator : closeInterpolator);

        return animationSet;
    }

    public static class Builder extends FloatingButtonAnimationHandlerBase.Builder<Builder, TranslateAlphaFloatingButtonAnimationHandler> {

        public Builder(FloatingButtonMenu floatingButtonMenu) {
            super(floatingButtonMenu);
        }

        @Override
        protected FloatingButtonAnimationHandlerBase makeAndSetSpecialProperties() {
            return new TranslateAlphaFloatingButtonAnimationHandler(mFloatingButtonMenu);
        }
    }
}
