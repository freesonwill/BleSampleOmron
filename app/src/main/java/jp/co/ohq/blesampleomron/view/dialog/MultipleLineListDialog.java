package jp.co.ohq.blesampleomron.view.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;

import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.model.entity.MultipleLineItem;
import jp.co.ohq.blesampleomron.view.adapter.MultipleLineListAdapter;

public class MultipleLineListDialog extends DialogFragment {

    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_ITEMS = "ARG_ITEMS";

    private String mTitle;
    private MultipleLineListAdapter mAdapter;

    public static MultipleLineListDialog newInstance(@NonNull String title, @NonNull MultipleLineItem[] items) {
        MultipleLineListDialog fragment = new MultipleLineListDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putParcelableArray(ARG_ITEMS, items);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mTitle = args.getString(ARG_TITLE);
        if (mTitle == null) {
            throw new IllegalArgumentException("Argument '" + ARG_TITLE + "' must not be null.");
        }

        MultipleLineItem[] items = (MultipleLineItem[]) args.getParcelableArray(ARG_ITEMS);
        if (items == null) {
            throw new IllegalArgumentException("Argument '" + ARG_ITEMS + "' must not be null.");
        }

        mAdapter = new MultipleLineListAdapter(getContext(), items);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.multiple_line_list, null);
        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setView(view)
                .setNegativeButton(getString(R.string.close), null)
                .create();
    }
}
