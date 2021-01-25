package jp.co.ohq.blesampleomron.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.ohq.blesampleomron.R;

public class TwoLineTextViewWithoutImage extends LinearLayout {

    private TextView mTitle;
    private TextView mSummary;
    private LinearLayout mLayout;

    public TwoLineTextViewWithoutImage(Context context) {
        this(context, null);
    }

    public TwoLineTextViewWithoutImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoLineTextViewWithoutImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TwoLineTextView, 0, 0);
        String title = a.getString(R.styleable.TwoLineTextView_title);
        String summary = a.getString(R.styleable.TwoLineTextView_summary);
        a.recycle();

        View rootView = LayoutInflater.from(context).inflate(R.layout.two_line_text_view_without_image, this);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mSummary = (TextView) rootView.findViewById(R.id.summary);
        mLayout = (LinearLayout) rootView.findViewById(R.id.layout_two_line);

        setTitle(title);
        setSummary(summary);
    }

    public void setTitle(@Nullable String title) {
        if (null != title) {
            mTitle.setText(title);
            mTitle.setVisibility(VISIBLE);
        } else {
            mTitle.setVisibility(GONE);
        }
    }

    public String getSummary() {
        return mSummary.getText().toString();
    }

    public void setSummary(@Nullable String summary) {
        if (null != summary) {
            mSummary.setText(summary);
            mSummary.setVisibility(VISIBLE);
        } else {
            mSummary.setVisibility(GONE);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mLayout.setEnabled(enabled);
        mTitle.setEnabled(enabled);
        mSummary.setEnabled(enabled);
    }
}
