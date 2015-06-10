package com.brachialste.earthquakemonitor.view.fab;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

/**
 * Created by brachialste on 17/02/15.
 */
public class MyFloatingActionsMenu extends FloatingActionsMenu {

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private boolean mHidden = false;
    /**
     * The FAB button's Y position when it is displayed.
     */
    private float mYDisplayed = -1;
    /**
     * The FAB button's Y position when it is hidden.
     */
    private float mYHidden = -1;

    public boolean isHidden() {
        return mHidden;
    }

    public MyFloatingActionsMenu(Context context) {
        super(context);

        WindowManager mWindowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            mYHidden = size.y;
        } else mYHidden = display.getHeight();
    }

    public MyFloatingActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);

        WindowManager mWindowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            mYHidden = size.y;
        } else mYHidden = display.getHeight();
    }

    public MyFloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        WindowManager mWindowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            mYHidden = size.y;
        } else mYHidden = display.getHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Perform the default behavior
        super.onLayout(changed, left, top, right, bottom);

        // Store the FAB button's displayed Y position if we are not already aware of it
        if (mYDisplayed == -1) {

            mYDisplayed = this.getY();
        }
    }

    public void hide(boolean hide) {
        // If the hidden state is being updated
        if (mHidden != hide) {

            // ocultamos los submenus en caso de que esten visibles
            if(isExpanded()){
                collapse();
            }

            // Store the new hidden state
            mHidden = hide;

            // Animate the FAB to it's new Y position
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "y", mHidden ? mYHidden : mYDisplayed).setDuration(500);
            animator.setInterpolator(mInterpolator);
            animator.start();
        }
    }

}
