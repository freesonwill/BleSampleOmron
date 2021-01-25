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

public class SectionWithSubject extends LinearLayout {

    private TextView mSubject;
    private TextView mDividerTop;
    private TextView mDividerBottom;

    public SectionWithSubject(Context context) {
        this(context, null);
    }

    public SectionWithSubject(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SectionWithSubject(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SectionWithSubject, 0, 0);
        String Subject = a.getString(R.styleable.SectionWithSubject_subject);
        Boolean dividerTopFlg = a.getBoolean(R.styleable.SectionWithSubject_divider_top, false);
        Boolean dividerBottomFlg = a.getBoolean(R.styleable.SectionWithSubject_divider_bottom, false);
        a.recycle();

        View rootView = LayoutInflater.from(context).inflate(R.layout.section_with_subject, this);
        mSubject = (TextView) rootView.findViewById(R.id.subject);
        mDividerTop = (TextView) rootView.findViewById(R.id.topDivider);
        mDividerBottom = (TextView) rootView.findViewById(R.id.bottomDivider);

        setSubject(Subject);
        setDividerTop(dividerTopFlg);
        setDividerBottom(dividerBottomFlg);
    }

    public void setSubject(@Nullable String subject) {
        if (null != subject) {
            mSubject.setText(subject);
            mSubject.setVisibility(VISIBLE);
        } else {
            mSubject.setVisibility(GONE);
        }
    }

    public void setDividerTop(boolean flg) {
        if (flg) {
            mDividerTop.setVisibility(VISIBLE);
        } else {
            mDividerTop.setVisibility(GONE);
        }
    }

    public void setDividerBottom(boolean flg) {
        if (flg) {
            mDividerBottom.setVisibility(VISIBLE);
        } else {
            mDividerBottom.setVisibility(GONE);
        }
    }
}
