package com.xxdroid.xman.ui.view;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.xxdroid.xman.R;

public class CategoryBar extends View {
    private static final String LOG_TAG = "CategoryBar";

    private static final int MARGIN = 12;

    private static final int ANI_TOTAL_FRAMES = 10;

    private static final int ANI_PERIOD = 100;
    private Timer timer;

    private class Category {
        public long value;

        public long tmpValue; // used for animation

        public long aniStep; // animation step

        public int resImg;
    }

    private ArrayList<Category> categories = new ArrayList<Category>();

    private long mFullValue;

    public void setFullValue(long value) {
        mFullValue = value;
    }

    public CategoryBar(Context context) {
        this(context, null);
    }

    public CategoryBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CategoryBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addCategory(int categoryImg) {
        Category ca = new Category();
        ca.resImg = categoryImg;
        categories.add(ca);
    }

    public boolean setCategoryValue(int index, long value) {
        if (index < 0 || index >= categories.size())
            return false;
        categories.get(index).value = value;
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable d = getDrawable(R.drawable.category_bar_empty);

        int width = getWidth() - MARGIN * 2;
        int height = getHeight() - MARGIN * 2;
        boolean isHorizontal = (width > height);

        Rect bounds = null;
        if ( isHorizontal )
            bounds = new Rect(MARGIN, 0, MARGIN + width, d.getIntrinsicHeight());
        else
            bounds = new Rect(0, MARGIN, d.getIntrinsicWidth(), MARGIN + height);

        int beginning = MARGIN;
        if ( !isHorizontal ) beginning += height;
        d.setBounds(bounds);
        d.draw(canvas);
        if (mFullValue != 0) {
            for (Category c : categories) {
                long value = (timer == null ? c.value : c.tmpValue);
                if ( isHorizontal ) {
                    int w = (int) (value * width / mFullValue);
                    if (w == 0)
                        continue;
                    bounds.left = beginning;
                    bounds.right = beginning + w;
                    d = getDrawable(c.resImg);
                    bounds.bottom = bounds.top + d.getIntrinsicHeight();
                    d.setBounds(bounds);
                    d.draw(canvas);
                    beginning += w;
                }
                else {
                    int h = (int) (value * height / mFullValue);
                    if (h == 0)
                        continue;
                    bounds.bottom = beginning;
                    bounds.top = beginning - h;
                    d = getDrawable(c.resImg);
                    bounds.right = bounds.left + d.getIntrinsicWidth();
                    d.setBounds(bounds);
                    d.draw(canvas);
                    beginning -= h;
                }
            }
        }
        if ( isHorizontal ) {
            bounds.left = 0;
            bounds.right = bounds.left + getWidth();
        }
        else {
            bounds.top = 0;
            bounds.bottom = bounds.top + getHeight();
        }
        d = getDrawable(R.drawable.category_bar_mask);
        d.setBounds(bounds);
        d.draw(canvas);
    }

    private Drawable getDrawable(int id) {
        return getContext().getResources().getDrawable(id);
    }

    private void stepAnimation() {
        if (timer == null)
            return;

        int finished = 0;
        for (Category c : categories) {
            c.tmpValue += c.aniStep;
            if (c.tmpValue >= c.value) {
                c.tmpValue = c.value;
                finished++;
                if (finished >= categories.size()) {
                    // stop animation
                    timer.cancel();
                    timer = null;
                    Log.v(LOG_TAG, "Animation stopped");
                    break;
                }
            }
        }

        postInvalidate();
    }

    synchronized public void startAnimation() {
        if (timer != null) {
            return;
        }

        Log.v(LOG_TAG, "startAnimation");

        for (Category c : categories) {
            c.tmpValue = 0;
            c.aniStep = c.value / ANI_TOTAL_FRAMES;
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                stepAnimation();
            }

        }, 0, ANI_PERIOD);
    }
}
