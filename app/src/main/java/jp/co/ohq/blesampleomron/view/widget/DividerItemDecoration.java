package jp.co.ohq.blesampleomron.view.widget;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    /**
     * Don't show any dividers.
     */
    public static final int SHOW_DIVIDER_NONE = 0;
    /**
     * Show a divider at the beginning of the group.
     */
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    /**
     * Show dividers between each item in the group.
     */
    public static final int SHOW_DIVIDER_MIDDLE = 1 << 1;
    /**
     * Show a divider at the end of the group.
     */
    public static final int SHOW_DIVIDER_END = 1 << 2;
    private Drawable mDivider;
    private int mShowDividers = SHOW_DIVIDER_MIDDLE;
    private int mDividerPadding;
    private int mDividerWidth;
    private int mDividerHeight;
    /**
     * @param divider Drawable that will divide each item.
     */
    public DividerItemDecoration(Drawable divider) {
        mDivider = divider;
        if (divider != null) {
            mDividerWidth = divider.getIntrinsicWidth();
            mDividerHeight = divider.getIntrinsicHeight();
        } else {
            mDividerWidth = 0;
            mDividerHeight = 0;
        }
    }

    /**
     * @param divider      Drawable that will divide each item.
     * @param showDividers One or more of {@link #SHOW_DIVIDER_BEGINNING},
     *                     {@link #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END},
     *                     or {@link #SHOW_DIVIDER_NONE} to show no dividers.
     */
    public DividerItemDecoration(Drawable divider, @DividerMode int showDividers) {
        this(divider);
        mShowDividers = showDividers;
    }

    /**
     * @param divider          Drawable that will divide each item.
     * @param showDividers     One or more of {@link #SHOW_DIVIDER_BEGINNING},
     *                         {@link #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END},
     *                         or {@link #SHOW_DIVIDER_NONE} to show no dividers.
     * @param dividerPaddingPx Padding of divider in pixel.
     */
    public DividerItemDecoration(Drawable divider, @DividerMode int showDividers, int dividerPaddingPx) {
        this(divider, showDividers);
        mDividerPadding = dividerPaddingPx;
    }

    /**
     * @return A flag set indicating how dividers should be shown around items.
     * @see #setShowDividers(int)
     */
    @DividerMode
    public int getShowDividers() {
        return mShowDividers;
    }

    /**
     * Set how dividers should be shown between items in this layout
     *
     * @param showDividers One or more of {@link #SHOW_DIVIDER_BEGINNING},
     *                     {@link #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END},
     *                     or {@link #SHOW_DIVIDER_NONE} to show no dividers.
     */
    public void setShowDividers(@DividerMode int showDividers) {
        mShowDividers = showDividers;
    }

    /**
     * @return the divider Drawable that will divide each item.
     * @see #setDividerDrawable(Drawable)
     */
    public Drawable getDividerDrawable() {
        return mDivider;
    }

    /**
     * Set a drawable to be used as a divider between items.
     *
     * @param divider Drawable that will divide each item.
     * @see #setShowDividers(int)
     */
    public void setDividerDrawable(Drawable divider) {
        if (divider == mDivider) {
            return;
        }
        mDivider = divider;
        if (divider != null) {
            mDividerWidth = divider.getIntrinsicWidth();
            mDividerHeight = divider.getIntrinsicHeight();
        } else {
            mDividerWidth = 0;
            mDividerHeight = 0;
        }
    }

    /**
     * Get the padding size used to inset dividers in pixels
     *
     * @see #setShowDividers(int)
     * @see #setDividerDrawable(Drawable)
     * @see #setDividerPadding(int)
     */
    public int getDividerPadding() {
        return mDividerPadding;
    }

    /**
     * Set padding displayed on both ends of dividers.
     *
     * @param padding Padding value in pixels that will be applied to each end
     * @see #setShowDividers(int)
     * @see #setDividerDrawable(Drawable)
     * @see #getDividerPadding()
     */
    public void setDividerPadding(int padding) {
        mDividerPadding = padding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mDivider == null || !(parent.getLayoutManager() instanceof LinearLayoutManager)) {
            return;
        }
        if (parent.getChildAdapterPosition(view) < 1) {
            return;
        }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            outRect.top += mDividerHeight;
        } else {
            outRect.left += mDividerWidth;
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final Drawable divider = this.mDivider;
        if (divider == null || !(parent.getLayoutManager() instanceof LinearLayoutManager)) {
            super.onDrawOver(c, parent, state);
            return;
        }

        // Initialization needed to avoid compiler warning
        int left = 0, right = 0, top = 0, bottom = 0, size;
        final int orientation = getOrientation(parent);
        final int childCount = parent.getChildCount();

        if (childCount > 0) {
            if (orientation == LinearLayoutManager.VERTICAL) {
                size = mDividerHeight;
                left = parent.getPaddingLeft() + mDividerPadding;
                right = parent.getWidth() - parent.getPaddingRight() - mDividerPadding;
            } else { //horizontal
                size = mDividerWidth;
                top = parent.getPaddingTop() + mDividerPadding;
                bottom = parent.getHeight() - parent.getPaddingBottom() - mDividerPadding;
            }

            final int showDividers = this.mShowDividers;

            // show beginning divider
            View child;
            RecyclerView.LayoutParams params;
            if ((showDividers & SHOW_DIVIDER_BEGINNING) == SHOW_DIVIDER_BEGINNING) {
                child = parent.getChildAt(0);
                params = (RecyclerView.LayoutParams) child.getLayoutParams();
                if (orientation == LinearLayoutManager.VERTICAL) {
                    top = child.getTop() - params.topMargin - size;
                    bottom = top + size;
                } else { // horizontal
                    left = child.getLeft() - params.leftMargin - size;
                    right = left + size;
                }
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }

            if ((showDividers & SHOW_DIVIDER_MIDDLE) == SHOW_DIVIDER_MIDDLE) {
                for (int i = 1; i < childCount; i++) {
                    child = parent.getChildAt(i);
                    params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    if (orientation == LinearLayoutManager.VERTICAL) {
                        top = child.getTop() - params.topMargin - size;
                        bottom = top + size;
                    } else { //horizontal
                        left = child.getLeft() - params.leftMargin - size;
                        right = left + size;
                    }
                    divider.setBounds(left, top, right, bottom);
                    divider.draw(c);
                }
            }

            // show end divider
            if ((showDividers & SHOW_DIVIDER_END) == SHOW_DIVIDER_END) {
                child = parent.getChildAt(childCount - 1);
                params = (RecyclerView.LayoutParams) child.getLayoutParams();
                if (orientation == LinearLayoutManager.VERTICAL) {
                    top = child.getBottom() + params.bottomMargin;
                    bottom = top + size;
                } else { // horizontal
                    left = child.getRight() + params.rightMargin;
                    right = left + size;
                }
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }

        }
    }

    private int getOrientation(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            return layoutManager.getOrientation();
        }
        return -1;
    }

    @IntDef(flag = true,
            value = {
                    SHOW_DIVIDER_NONE,
                    SHOW_DIVIDER_BEGINNING,
                    SHOW_DIVIDER_MIDDLE,
                    SHOW_DIVIDER_END
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DividerMode {
    }
}