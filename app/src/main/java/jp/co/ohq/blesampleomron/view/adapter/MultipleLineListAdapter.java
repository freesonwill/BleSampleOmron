package jp.co.ohq.blesampleomron.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.model.entity.MultipleLineItem;

public class MultipleLineListAdapter extends AbstractListAdapter<MultipleLineItem> {

    public MultipleLineListAdapter(@NonNull Context context) {
        super(context);
    }

    public MultipleLineListAdapter(@NonNull Context context, @NonNull MultipleLineItem[] items) {
        super(context, items);
    }

    public MultipleLineListAdapter(@NonNull Context context, @NonNull List<MultipleLineItem> items) {
        super(context, items);
    }

    @NonNull
    @Override
    protected View onCreateView(int position, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item_multiple_line, parent, false);

        LinearLayout categoryLayout = (LinearLayout) view.findViewById(R.id.category_layout);
        LinearLayout lineLayout = (LinearLayout) view.findViewById(R.id.line_layout);
        TextView category = (TextView) view.findViewById(R.id.category);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView summary = (TextView) view.findViewById(R.id.summary);
        ViewHolder holder = new ViewHolder(categoryLayout, lineLayout, category, title, summary);

        view.setTag(holder);
        return view;
    }

    @Override
    protected void onBindView(int position, @NonNull View view) {
        MultipleLineItem item = getItem(position);
        ViewHolder holder = (ViewHolder) view.getTag();

        if (!TextUtils.isEmpty(item.getCategory())) {
            holder.setCategory(item.getCategory());

        } else if (!TextUtils.isEmpty(item.getTitle())) {
            holder.setLine(item.getTitle(), item.getSummary());

        } else {
            throw new AndroidRuntimeException("Data value is unable to support.");
        }
    }

    private static class ViewHolder {

        private final LinearLayout mCategoryLayout;
        private final LinearLayout mLineLayout;
        private final TextView mCategory;
        private final TextView mTitle;
        private final TextView mSummary;

        ViewHolder(LinearLayout categoryLayout, LinearLayout lineLayout, TextView category, TextView title, TextView summary) {
            mCategoryLayout = categoryLayout;
            mLineLayout = lineLayout;
            mCategory = category;
            mTitle = title;
            mSummary = summary;
        }

        void setCategory(@NonNull String category) {
            mCategoryLayout.setVisibility(View.VISIBLE);
            mCategory.setText(category);

            mLineLayout.setVisibility(View.GONE);
        }

        void setLine(@NonNull String title, @Nullable String summary) {
            mCategoryLayout.setVisibility(View.GONE);

            mLineLayout.setVisibility(View.VISIBLE);
            mTitle.setText(title);

            if (!TextUtils.isEmpty(summary)) {
                mSummary.setVisibility(View.VISIBLE);
                mSummary.setText(summary);
            } else {
                mSummary.setVisibility(View.GONE);
            }
        }
    }
}
