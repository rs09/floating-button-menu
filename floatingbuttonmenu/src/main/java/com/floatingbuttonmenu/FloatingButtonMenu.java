package com.floatingbuttonmenu;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.floatingbuttonmenu.animation.FloatingButtonAnimationHandlerBase;
import com.floatingbuttonmenu.animation.TranslateAlphaFloatingButtonAnimationHandler;

/**
 * <p>A menu that emulates android's floating action button with the additional feature of opening up sub-items
 * in an  arc when clicked.</p>
 * <p/>
 * <p>Items should be added using the {@link #addItem(android.view.View)} method. All property method calls can be
 * chained together.</p>
 * <p/>
 * <p>The menu also provides listeners for sub-item clicks ({@link com.floatingbuttonmenu.FloatingButtonMenu.OnItemClickListener}) and for
 * when the menu is opened/closed ({@link com.floatingbuttonmenu.FloatingButtonMenu.OnStateChangeListener})</p>
 * <p/>
 * <p>Ideally, this layout should match the parent and be on top of all views. Android padding and margins should also be avoided since
 * that can cause clipping of the background.</p>
 */
public class FloatingButtonMenu extends RelativeLayout {

    /**
     * Listener for notifying menu item clicks.
     */
    public interface OnItemClickListener {
        /**
         * This method is called when an item in the menu has been clicked.
         *
         * @param childView: the view of the child that was clicked
         * @param index:     the index of the child that was clicked
         */
        public void onItemClick(View childView, int index);
    }

    /**
     * Listener for notifying when the menu has opened/closed.
     */
    public interface OnStateChangeListener {
        /**
         * This method is called when the menu has opened/closed.
         *
         * @param opened: whether the menu is currently open or not.
         */
        public void onMenuStateChanged(boolean opened);
    }

    private FloatingButtonLayout floatingButtonLayout;
    private ImageView floatingButton;
    private View backgroundView;
    private ViewGroup floatingButtonContainer;
    private OnItemClickListener onItemClickListener;
    private OnStateChangeListener onStateChangeListener;

    private boolean showBackground = true;

    FloatingButtonMenu(Context context) {
        super(context);
        init(null);
    }

    public FloatingButtonMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.floating_button_menu, this);

        floatingButtonLayout = (FloatingButtonLayout) findViewById(R.id.floating_button_layout);
        floatingButtonContainer = (ViewGroup) findViewById(R.id.floating_button_container);
        backgroundView = findViewById(R.id.background_view);
        floatingButton = (ImageView) findViewById(R.id.floating_button);
        floatingButton.setOnClickListener(floatButtonClickListener);

        setAnimationHandler(new TranslateAlphaFloatingButtonAnimationHandler.Builder(this).build());

        /*
        These properties are being set so that we can intercept the key events (mainly the back press event)
         */
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.requestFocus();

        applyAttrs(attrs);
        setFloatingButtonParams();
    }

    private void applyAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingButton, 0, 0);

        float fromDegree = ta.getFloat(R.styleable.FloatingButton_fromDegrees, FloatingButtonLayout.FROM_DEGREES_DEFAULT);
        float toDegree = ta.getFloat(R.styleable.FloatingButton_toDegrees, FloatingButtonLayout.TO_DEGREES_DEFAULT);
        int radius = ta.getDimensionPixelOffset(R.styleable.FloatingButton_radius, FloatingButtonLayout.DEFAULT_RADIUS);
        int width = ta.getDimensionPixelOffset(R.styleable.FloatingButton_button_width, 0);
        int height = ta.getDimensionPixelOffset(R.styleable.FloatingButton_button_height, 0);

        int srcResId = ta.getResourceId(R.styleable.FloatingButton_fg, 0);
        int backgroundResId = ta.getResourceId(R.styleable.FloatingButton_bg, 0);

        int backgroundColor = ta.getColor(R.styleable.FloatingButton_background_color, 0);
        boolean showBackground = ta.getBoolean(R.styleable.FloatingButton_show_background, true);

        if (ta.hasValue(R.styleable.FloatingButton_button_margin)) {
            int margin = ta.getDimensionPixelOffset(R.styleable.FloatingButton_button_margin, 0);
            setFloatingButtonMargin(margin);
        } else {
            int marginLeft = ta.getDimensionPixelOffset(R.styleable.FloatingButton_button_margin_left, 0);
            int marginTop = ta.getDimensionPixelOffset(R.styleable.FloatingButton_button_margin_top, 0);
            int marginRight = ta.getDimensionPixelOffset(R.styleable.FloatingButton_button_margin_right, 0);
            int marginBottom = ta.getDimensionPixelOffset(R.styleable.FloatingButton_button_margin_bottom, 0);

            setFloatingButtonMargin(marginLeft, marginTop, marginRight, marginBottom);
        }

        setArc(fromDegree, toDegree)
                .setFloatingButtonSize(width, height, false)
                .setRadius(radius)
                .setButtonForeground(srcResId)
                .setButtonBackground(backgroundResId)
                .setBackground(backgroundColor)
                .setShowBackground(showBackground);

        ta.recycle();
    }

    private void setFloatingButtonParams() {
        /*
        Ensuring the floatingButton is inflated before we get it's location on screen.
         */
        floatingButton.post(new Runnable() {
            @Override
            public void run() {
                Point coordinates = getFloatingButtonCoordinates();

                floatingButtonLayout.setFloatingButtonParams(coordinates, floatingButton.getMeasuredWidth(), floatingButton.getMeasuredHeight());
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isExpanded() && floatingButtonLayout.isPointOutsideFloatingButtonLayout((int) ev.getX(), (int) ev.getY())) {
                close();
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (isExpanded()) {
                close();
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    /**
     * Gets the coordinates of the main action view
     * This method should only be called after the main layout of the Activity is drawn.
     *
     * @return a Point containing x and y coordinates of the top left corner of floating button
     */
    private Point getFloatingButtonCoordinates() {
        int[] coords = new int[2];

        /*
         This method returns a x and y values that can be larger than the dimensions of the device screen.
          */
        floatingButton.getLocationOnScreen(coords);
        Rect activityFrame = new Rect();

        View mainContentView = getMainContentView();
        mainContentView.getWindowVisibleDisplayFrame(activityFrame);
        /*
         So, we need to deduce the offsets.
          */
        coords[0] -= (getScreenSize().x - getMainContentView().getMeasuredWidth());
        coords[1] -= (activityFrame.height() + activityFrame.top - mainContentView.getMeasuredHeight());
        return new Point(coords[0], coords[1]);
    }

    /**
     * Returns a Rect containing the area that a child is to be contained in.
     *
     * @param centerX:     the x coordinate of the center of the FloatingButton
     * @param centerY:     the y coordinate of the center of the FloatingButton
     * @param radius:      the radius of the arc
     * @param degrees:     the degrees at which the child is to be placed on the arc
     * @param childWidth:  width of the child
     * @param childHeight: height of the child
     * @return
     */
    public static Rect computeChildFrame(final int centerX, final int centerY, final int radius, final float degrees, final int childWidth, final int childHeight) {
        final double childCenterX = centerX + radius * Math.cos(Math.toRadians(degrees));
        final double childCenterY = centerY + radius * Math.sin(Math.toRadians(degrees));

        return new Rect((int) (childCenterX - childWidth / 2), (int) (childCenterY - childHeight / 2),
                (int) (childCenterX + childWidth / 2), (int) (childCenterY + childHeight / 2));
    }

    /**
     * Retrieves the screen size from the Activity context
     *
     * @return the screen size as a Point object
     */
    private Point getScreenSize() {
        Point size = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(size);
        return size;
    }

    /**
     * Finds and returns the main content view from the Activity context.
     *
     * @return the main content view
     */
    public View getMainContentView() {
        return ((Activity) getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
    }

    private OnClickListener floatButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            floatingButtonLayout.toggleState(true);
        }
    };

    /**
     * Opens the menu. It is animated while opening
     */
    public void open() {
        open(true);
    }

    /**
     * Opens the menu
     *
     * @param animate: whether the menu should be animated while opening.
     */
    public void open(boolean animate) {
        floatingButtonLayout.setState(true, animate);
    }

    /**
     * Closes the menu. It is animated while closing.
     */
    public void close() {
        close(true);
    }

    /**
     * Closes the menu
     *
     * @param animate: whether the menu should be animated while closing.
     */
    public void close(boolean animate) {
        floatingButtonLayout.setState(false, animate);
    }

    /**
     * Toggles the current state of the menu. Animates the opening/closing.
     */
    public void toggle() {
        toggle(true);
    }

    /**
     * Toggles the current state of the menu.
     *
     * @param animate: whether the opening/closing of the menu should be animated.
     */
    public void toggle(boolean animate) {
        floatingButtonLayout.toggleState(animate);
    }

    /**
     * Sets a click listener for every child view added to the FloatingButtonLayout. This way we can trigger
     * the FloatingButtonMenuItemClickListener when any of these items are clicked.
     *
     * @param childView
     * @param index
     */
    private void setOnClickListenerForChild(View childView, final int index) {
        childView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, index);
                }
                close();
            }
        });
    }

    /**
     * Add a new view to the FloatingButtonMenu
     *
     * @param view
     */
    public FloatingButtonMenu addItem(View view) {
        int childCount = floatingButtonLayout.getChildCount();
        floatingButtonLayout.addView(view);
        /*
        The child count will be the index for this view
         */
        setOnClickListenerForChild(view, childCount);

        return this;
    }

    public FloatingButtonMenu setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.onItemClickListener = itemClickListener;
        return this;
    }

    public FloatingButtonMenu setAnimationHandler(FloatingButtonAnimationHandlerBase animationHandler) {
        floatingButtonLayout.setAnimationHandler(animationHandler);
        return this;
    }

    public FloatingButtonMenu setOnStateChangeListener(OnStateChangeListener menuStateListener) {
        this.onStateChangeListener = menuStateListener;
        return this;
    }

    public FloatingButtonMenu setArc(float fromDegrees, float toDegrees) {
        floatingButtonLayout.setArc(fromDegrees, toDegrees);
        return this;
    }

    public FloatingButtonMenu setFloatingButtonSize(int width, int height) {
        return setFloatingButtonSize(width, height, true);
    }

    /**
     * Set the size for the floating button.
     * @param width
     * @param height
     * @param setParamsInFloatingButtonLayout: true if we want the FloatingButtonLayout to refresh the current param values for
     *                                       the floating button. This causes a redraw of the layout. This should only be false
     *                                       in cases where we know the param values are going to be refreshed eventually.
     * @return
     */
    private FloatingButtonMenu setFloatingButtonSize(int width, int height, boolean setParamsInFloatingButtonLayout) {
        /*
        Both width and height should be greater than zero
         */
        if (width <= 0 || height <= 0) {
            return this;
        }

        LayoutParams layoutParams = (LayoutParams) floatingButton.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        floatingButton.setLayoutParams(layoutParams);

        if (setParamsInFloatingButtonLayout) {
            setFloatingButtonParams();
        }
        return this;
    }

    public FloatingButtonMenu setFloatingButtonMargin(int margin) {
        return setFloatingButtonMargin(margin, margin, margin, margin);
    }

    public FloatingButtonMenu setFloatingButtonMargin(int left, int top, int right, int bottom) {
        if (floatingButtonContainer.getPaddingLeft() == left && floatingButtonContainer.getPaddingTop() == top
                && floatingButtonContainer.getPaddingRight() == right && floatingButtonContainer.getPaddingBottom() == bottom) {
            /*
            No change
             */
            return this;
        }
        floatingButtonContainer.setPadding(left, top, right, bottom);
        floatingButtonLayout.requestLayout();
        return this;
    }

    public FloatingButtonMenu setRadius(int radius) {
        floatingButtonLayout.setRadius(radius);
        return this;
    }

    public FloatingButtonMenu setButtonForeground(int resId) {
        floatingButton.setImageResource(resId);
        return this;
    }

    public FloatingButtonMenu setButtonBackground(int resId) {
        floatingButton.setBackgroundResource(resId);
        return this;
    }

    public FloatingButtonMenu setBackground(int color) {
        backgroundView.setBackgroundColor(color);
        return this;
    }

    public FloatingButtonMenu setShowBackground(boolean show) {
        if (showBackground == show) {
            return this;
        }
        showBackground = show;
        /*
        If the background is to be shown, we set the visibility based on what the current state of the
        menu is. If not, we simply make it invisible.
         */
        if (showBackground) {
            backgroundView.setVisibility(isExpanded() ? VISIBLE : INVISIBLE);
        } else {
            backgroundView.setVisibility(INVISIBLE);
        }
        return this;
    }

    public float getToDegrees() {
        return floatingButtonLayout.getToDegrees();
    }

    public float getFromDegrees() {
        return floatingButtonLayout.getFromDegrees();
    }

    public Point getFloatingButtonTopCornerCoor() {
        return floatingButtonLayout.getFloatingButtonTopCornerCoor();
    }

    public Point getFloatingButtonCenter() {
        return floatingButtonLayout.getFloatingButtonCenter();
    }

    public boolean isExpanded() {
        return floatingButtonLayout.isExpanded();
    }

    public int getRadius() {
        return floatingButtonLayout.getRadius();
    }

    public int getMenuChildCount() {
        return floatingButtonLayout.getChildCount();
    }

    public View getMenuChildAt(int index) {
        return floatingButtonLayout.getChildAt(index);
    }

    public OnStateChangeListener getOnStateChangeListener() {
        return onStateChangeListener;
    }

    public View getBackgroundView() {
        return backgroundView;
    }

    public boolean shouldShowBackground() {
        return showBackground;
    }
}
