/*
 * android-spinnerwheel
 * https://github.com/ai212983/android-spinnerwheel
 *
 * based on
 *
 * Android Wheel Control.
 * https://code.google.com/p/android-wheel/
 *
 * Copyright 2011 Yuri Kanivets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package antistatic.widget.wheel;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;


/**
 * Abstract spinner widget view.
 * This class should be subclassed.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public abstract class AbstractWheelView extends AbstractWheel {

    private static int itemID = -1;

    @SuppressWarnings("unused")
    private final String LOG_TAG = AbstractWheelView.class.getName() + " #" + (++itemID);


    /**
     * The {@link android.graphics.Paint} for drawing the selector.
     */
    protected Paint mSelectorWheelPaint;

    /**
     * Divider for showing item to be selected while scrolling
     */
    protected Drawable mSelectionDivider;

    /**
     * The height of the selection divider.
     */
    protected int mSelectionDividerWidth;

    /**
     * The height of a selector element (text + gap).
     */
    protected int mSelectorElementHeight;

    /**
     * The {@link android.graphics.Paint} for drawing the separators.
     */
    protected Paint mSeparatorsPaint;

    /**
     * {@link com.nineoldandroids.animation.Animator} for dimming the selector widget.
     */
    protected Animator mDimSelectorWheelAnimator;

    /**
     * {@link com.nineoldandroids.animation.Animator} for dimming the selector widget.
     */
    protected Animator mDimSeparatorsAnimator;

    /**
     * The property for setting the selector paint.
     */
    protected static final String PROPERTY_SELECTOR_PAINT_COEFF = "selectorPaintCoeff";

    /**
     * The property for setting the separators paint.
     */
    protected static final String PROPERTY_SEPARATORS_PAINT_ALPHA = "separatorsPaintAlpha";

    /**
     * The alpha of the selector widget when it is dimmed.
     */
    protected static final int SELECTOR_WHEEL_DIM_ALPHA = 30; // 60 in ICS //TODO: Make this parameter customizable

    /**
     * The alpha of separators widget when they are shown.
     */
    protected static final int SEPARATORS_BRIGHT_ALPHA = 70; //TODO: Make this parameter customizable

    /**
     * The alpha of separators when they are is dimmed.
     */
    protected static final int SEPARATORS_DIM_ALPHA = 70; //TODO: Make this parameter customizable

    /** Top and bottom items offset */
    protected static final int ITEM_OFFSET_PERCENT = 10;

    /** Left and right padding value */
    protected static final int PADDING = 10;

    protected Bitmap mSpinBitmap;
    protected Bitmap mSeparatorsBitmap;


    //--------------------------------------------------------------------------
    //
    //  Constructors
    //
    //--------------------------------------------------------------------------

    public AbstractWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context);
    }

    public AbstractWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    /**
     * Constructor
     * @param context Context for creation
     */
    public AbstractWheelView(Context context) {
        super(context);
        initData(context);
    }

    //--------------------------------------------------------------------------
    //
    //  Initiating assets and setters for paints
    //
    //--------------------------------------------------------------------------

    @Override
    protected void initData(Context context) {
        super.initData(context);

        // creating animators
        mDimSelectorWheelAnimator = ObjectAnimator.ofFloat(this, PROPERTY_SELECTOR_PAINT_COEFF, 1, 0);

        mDimSeparatorsAnimator = ObjectAnimator.ofInt(this, PROPERTY_SEPARATORS_PAINT_ALPHA,
                SEPARATORS_BRIGHT_ALPHA, SEPARATORS_DIM_ALPHA
        );

        // creating paints
        mSeparatorsPaint = new Paint();
        mSeparatorsPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mSeparatorsPaint.setAlpha(SEPARATORS_DIM_ALPHA);

        mSelectorWheelPaint = new Paint();
        mSelectorWheelPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    /**
     * Recreates assets (like bitmaps) when layout size has been changed
     *
     * @param width New widget width
     * @param height New widget height
     */
    @Override
    protected void recreateAssets(int width, int height) {
        mSpinBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mSeparatorsBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mSelectorElementHeight = getItemDimension();
        setSelectorPaintCoeff(0);
    }

    /**
     * Sets the <code>alpha</code> of the {@link Paint} for drawing separators
     * widget.
     * @param alpha
     */
    @SuppressWarnings("unused")  // Called via reflection
    public void setSeparatorsPaintAlpha(int alpha) {
        mSeparatorsPaint.setAlpha(alpha);
        invalidate();
    }

    /**
     * Sets the <code>coeff</code> of the {@link Paint} for drawing
     * the selector widget.
     *
     * @param coeff Coefficient from 0 (selector is passive) to 1 (selector is active)
     */
    abstract public void setSelectorPaintCoeff(float coeff);


    //--------------------------------------------------------------------------
    //
    //  Processing scroller events
    //
    //--------------------------------------------------------------------------

    @Override
    protected void onScrollTouched() {
        mDimSelectorWheelAnimator.cancel();
        mDimSeparatorsAnimator.cancel();
        setSelectorPaintCoeff(1);
        setSeparatorsPaintAlpha(SEPARATORS_BRIGHT_ALPHA);
    }

    @Override
    protected void onScrollTouchedUp() {
        fadeSelectorWheel(750);
        lightSeparators(750);
    }

    @Override
    protected void onScrollFinished() {
        fadeSelectorWheel(500);
        lightSeparators(500);
    }

    //----------------------------------
    //  Animating components
    //----------------------------------

    /**
     * Fade the selector widget via an animation.
     *
     * @param animationDuration The duration of the animation.
     */
    private void fadeSelectorWheel(long animationDuration) {
        mDimSelectorWheelAnimator.setDuration(animationDuration);
        mDimSelectorWheelAnimator.start();
    }

    /**
     * Fade the selector widget via an animation.
     *
     * @param animationDuration The duration of the animation.
     */
    private void lightSeparators(long animationDuration) {
        mDimSeparatorsAnimator.setDuration(animationDuration);
        mDimSeparatorsAnimator.start();
    }


    //--------------------------------------------------------------------------
    //
    //  Layout measuring
    //
    //--------------------------------------------------------------------------

    /**
     * Perform layout measurements
     */
    abstract protected void measureLayout();


    //--------------------------------------------------------------------------
    //
    //  Drawing stuff
    //
    //--------------------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
            if (rebuildItems()) {
                measureLayout();
                doItemsLayout();
            }
            doItemsLayout();
            drawItems(canvas);
        }
    }

    /**
     * Draws items on specified canvas
     *
     * @param canvas the canvas for drawing
     */
    abstract protected void drawItems(Canvas canvas);
}