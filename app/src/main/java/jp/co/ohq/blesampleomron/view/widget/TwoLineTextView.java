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

public class TwoLineTextView extends LinearLayout {

    private TextView mTitle;
    private TextView mSummary;

    public TwoLineTextView(Context context) {
        this(context, null);
    }

    public TwoLineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoLineTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TwoLineTextView, 0, 0);
        String title = a.getString(R.styleable.TwoLineTextView_title);
        String summary = a.getString(R.styleable.TwoLineTextView_summary);
        a.recycle();

        View rootView = LayoutInflater.from(context).inflate(R.layout.two_line_text_view, this);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mSummary = (TextView) rootView.findViewById(R.id.summary);

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

    public void setSummary(@Nullable String summary) {
        if (null != summary) {
            mSummary.setText(summary);
            mSummary.setVisibility(VISIBLE);
        } else {
            mSummary.setVisibility(GONE);
        }
    }
}
