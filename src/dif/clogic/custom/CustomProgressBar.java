package dif.clogic.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewParent;
import dif.clogic.app.R;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 10. 27.
 * Time: 오전 2:29
 * To change this template use File | Settings | File Templates.
 */
public class CustomProgressBar extends View {

    private static final int MAX_LEVEL = 10000;

    int mMinWidth;
    int mMaxWidth;
    int mMinHeight;
    int mMaxHeight;

    int widthSize;
    int heightSize;

    private int mProgress;
    private int mSecondaryProgress;
    private int mMax;

    private Drawable mProgressDrawable;
    private Drawable mCurrentDrawable;
    Bitmap mSampleTile;
    private boolean mNoInvalidate;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    private long mUiThreadId;

    private boolean mInDrawing;

    protected int mScrollX;
    protected int mScrollY;
    protected int mPaddingLeft;
    protected int mPaddingRight;
    protected int mPaddingTop;
    protected int mPaddingBottom;
    protected ViewParent mParent;

    public CustomProgressBar(Context context) {
        super(context);
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mUiThreadId = Thread.currentThread().getId();
        initProgressBar();

        mNoInvalidate = true;

        Drawable drawable = context.getResources().getDrawable(R.drawable.custom_seekbar);
        setProgressDrawable(drawable);

        mMax = 0;
        mProgress = 0;

        widthSize = 0;
        heightSize = 0;

        mNoInvalidate = false;
    }

    /**
     * <p>
     * Initialize the progress bar's default values:
     * </p>
     * <ul>
     * <li>progress = 0
     * </li><li>max = 100
     * </li></ul>
     */
    private void initProgressBar() {
        mMax = 100;
        mProgress = 0;
        mSecondaryProgress = 0;
        mMinWidth = 24;
        mMaxWidth = 48;
        mMinHeight = 24;
        mMaxHeight = 48;
    }

    /**
     * <p>Get the drawable used to draw the progress bar in
     * progress mode.</p>
     *
     * @return a {@link android.graphics.drawable.Drawable} instance
     *
     * @see #setProgressDrawable(android.graphics.drawable.Drawable)
     */
    public Drawable getProgressDrawable() {
        return mProgressDrawable;
    }

    /**
     * <p>Define the drawable used to draw the progress bar in
     * progress mode.</p>
     *
     * @param d the new drawable
     *
     * @see #getProgressDrawable()
     */
    public void setProgressDrawable(Drawable d) {
        if (d != null) {
            d.setCallback(this);
            // Make sure the ProgressBar is always tall enough
            int drawableHeight = d.getMinimumHeight();
            if (mMaxHeight < drawableHeight) {
                mMaxHeight = drawableHeight;
                requestLayout();
            }
        }
        mProgressDrawable = d;
        mCurrentDrawable = d;
        postInvalidate();
    }

    /**
     * @return The drawable currently used to draw the progress bar
     */
    Drawable getCurrentDrawable() {
        return mCurrentDrawable;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mProgressDrawable || super.verifyDrawable(who);
    }

    @Override
    public void postInvalidate() {
        if (!mNoInvalidate) {
            super.postInvalidate();
        }
    }

    private class RefreshProgressRunnable implements Runnable {

        private int mId;
        private int mProgress;
        private boolean mFromUser;

        RefreshProgressRunnable(int id, int progress, boolean fromUser) {
            mId = id;
            mProgress = progress;
            mFromUser = fromUser;
        }

        public void run() {
            doRefreshProgress(mId, mProgress, mFromUser);
            // Put ourselves back in the cache when we are done
            mRefreshProgressRunnable = this;
        }

        public void setup(int id, int progress, boolean fromUser) {
            mId = id;
            mProgress = progress;
            mFromUser = fromUser;
        }

    }

    private synchronized void doRefreshProgress(int id, int progress, boolean fromUser) {
        float scale = mMax > 0 ? (float) progress / (float) mMax : 0;
        final Drawable d = mCurrentDrawable;
        if (d != null) {
            Drawable progressDrawable = null;

            if (d instanceof LayerDrawable) {
                progressDrawable = ((LayerDrawable) d).findDrawableByLayerId(id);
            }

            final int level = (int) (scale * MAX_LEVEL);
            (progressDrawable != null ? progressDrawable : d).setLevel(level);
        } else {
            invalidate();
        }

        if (id == android.R.id.progress) {
            onProgressRefresh(scale, fromUser);
        }
    }

    void onProgressRefresh(float scale, boolean fromUser) {
    }

    private synchronized void refreshProgress(int id, int progress, boolean fromUser) {
        if (mUiThreadId == Thread.currentThread().getId()) {
            doRefreshProgress(id, progress, fromUser);
        } else {
            RefreshProgressRunnable r;
            if (mRefreshProgressRunnable != null) {
                // Use cached RefreshProgressRunnable if available
                r = mRefreshProgressRunnable;
                // Uncache it
                mRefreshProgressRunnable = null;
                r.setup(id, progress, fromUser);
            } else {
                // Make a new one
                r = new RefreshProgressRunnable(id, progress, fromUser);
            }
            post(r);
        }
    }

    /**
     * <p>Set the current progress to the specified value.</p>
     *
     * @param progress the new progress, between 0 and {@link #getMax()}
     *
     * @see #getProgress()
     * @see #incrementProgressBy(int)
     */
    public synchronized void setProgress(int progress) {
        setProgress(progress, false);
    }

    synchronized void setProgress(int progress, boolean fromUser) {
        if (progress < 0) {
            progress = 0;
        }

        if (progress > mMax) {
            progress = mMax;
        }

        if (progress != mProgress) {
            mProgress = progress;
            refreshProgress(android.R.id.progress, mProgress, fromUser);
        }
    }

    /**
     * <p>
     * Set the current secondary progress to the specified value.
     * </p>
     *
     * @param secondaryProgress the new secondary progress, between 0 and {@link #getMax()}
     * @see #getSecondaryProgress()
     * @see #incrementSecondaryProgressBy(int)
     */
    public synchronized void setSecondaryProgress(int secondaryProgress) {
        if (secondaryProgress < 0) {
            secondaryProgress = 0;
        }

        if (secondaryProgress > mMax) {
            secondaryProgress = mMax;
        }

        if (secondaryProgress != mSecondaryProgress) {
            mSecondaryProgress = secondaryProgress;
            refreshProgress(android.R.id.secondaryProgress, mSecondaryProgress, false);
        }
    }

    /**
     * <p>Get the progress bar's current level of progress.</p>
     *
     * @return the current progress, between 0 and {@link #getMax()}
     *
     * @see #setProgress(int)
     * @see #setMax(int)
     * @see #getMax()
     */
    @ViewDebug.ExportedProperty
    public synchronized int getProgress() {
        return mProgress;
    }

    /**
     * <p>Get the progress bar's current level of secondary progress.</p>
     *
     * @return the current secondary progress, between 0 and {@link #getMax()}
     *
     * @see #setSecondaryProgress(int)
     * @see #setMax(int)
     * @see #getMax()
     */
    @ViewDebug.ExportedProperty
    public synchronized int getSecondaryProgress() {
        return mSecondaryProgress;
    }

    /**
     * <p>Return the upper limit of this progress bar's range.</p>
     *
     * @return a positive integer
     *
     * @see #setMax(int)
     * @see #getProgress()
     * @see #getSecondaryProgress()
     */
    @ViewDebug.ExportedProperty
    public synchronized int getMax() {
        return mMax;
    }

    /**
     * <p>Set the range of the progress bar to 0...<tt>max</tt>.</p>
     *
     * @param max the upper range of this progress bar
     *
     * @see #getMax()
     * @see #setProgress(int)
     * @see #setSecondaryProgress(int)
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != mMax) {
            mMax = max;
            postInvalidate();

            if (mProgress > max) {
                mProgress = max;
                refreshProgress(android.R.id.progress, mProgress, false);
            }
        }
    }

    /**
     * <p>Increase the progress bar's progress by the specified amount.</p>
     *
     * @param diff the amount by which the progress must be increased
     *
     * @see #setProgress(int)
     */
    public synchronized final void incrementProgressBy(int diff) {
        setProgress(mProgress + diff);
    }

    /**
     * <p>Increase the progress bar's secondary progress by the specified amount.</p>
     *
     * @param diff the amount by which the secondary progress must be increased
     *
     * @see #setSecondaryProgress(int)
     */
    public synchronized final void incrementSecondaryProgressBy(int diff) {
        setSecondaryProgress(mSecondaryProgress + diff);
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);
        }
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (!mInDrawing) {
            if (verifyDrawable(dr)) {
                final Rect dirty = dr.getBounds();
                final int scrollX = mScrollX + mPaddingLeft;
                final int scrollY = mScrollY + mPaddingTop;

                invalidate(dirty.left + scrollX, dirty.top + scrollY,
                        dirty.right + scrollX, dirty.bottom + scrollY);
            } else {
                super.invalidateDrawable(dr);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // onDraw will translate the canvas so we draw starting at 0,0
        int right = w - mPaddingRight - mPaddingLeft;
        int bottom = h - mPaddingBottom - mPaddingTop;

        if (mProgressDrawable != null) {
            mProgressDrawable.setBounds(0, 0, right, bottom);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable d = mCurrentDrawable;
        if (d != null) {
            // Translate canvas so a indeterminate circular progress bar with padding
            // rotates properly in its animation
            canvas.save();
            canvas.translate(mPaddingLeft, mPaddingTop);
            d.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = mCurrentDrawable;

        int dw = 0;
        int dh = 0;
        if (d != null) {
            dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
            dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
        }
        dw += mPaddingLeft + mPaddingRight;
        dh += mPaddingTop + mPaddingBottom;

        setMeasuredDimension(resolveSize(dw, widthMeasureSpec),
                resolveSize(dh, heightMeasureSpec));
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        int[] state = getDrawableState();

        if (mProgressDrawable != null && mProgressDrawable.isStateful()) {
            mProgressDrawable.setState(state);
        }
    }
}