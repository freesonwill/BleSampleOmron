package jp.co.ohq.blesampleomron.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import jp.co.ohq.blesampleomron.R;
import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.blesampleomron.model.entity.HistoryData;
import jp.co.ohq.blesampleomron.model.system.HistoryManager;
import jp.co.ohq.blesampleomron.view.dialog.SimpleDialog;
import jp.co.ohq.utility.Bundler;
import jp.co.ohq.utility.StringEx;

import static jp.co.ohq.blesampleomron.R.string.history;

public class HistoryFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<List<HistoryData>>,
        SimpleDialog.Callback {

    private static final int LOADER_ID_LOAD_HISTORY = 1;
    private static final int LOADER_ID_REMOVE_HISTORY = 2;

    private static final int DIALOG_REQ_CODE_DELETE = 1;
    private EventListener mListener;
    private ProgressBar mProgressBar;
    private ScrollView mHistoryTableLayout;
    private TableLayout mTableLayout;
    @NonNull
    private List<HistoryData> mHistoryDataList = new LinkedList<>();

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof EventListener)) {
            throw new AndroidRuntimeException("Activity is must be implement 'EventListener'");
        }
        mListener = (EventListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        mHistoryTableLayout = (ScrollView) rootView.findViewById(R.id.history_table_layout);
        mTableLayout = (TableLayout) rootView.findViewById(R.id.history_table);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        LoaderManager.getInstance(this).restartLoader(LOADER_ID_LOAD_HISTORY, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_history, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.delete);
        if (0 == mHistoryDataList.size()) {
            item.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                new SimpleDialog.Builder(this)
                        .message(getString(R.string.delete_history_message))
                        .positive(getString(R.string.ok))
                        .negative(getString(R.string.cancel))
                        .requestCode(DIALOG_REQ_CODE_DELETE)
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<HistoryData>> onCreateLoader(int id, Bundle args) {
        Loader<List<HistoryData>> loader = null;
        switch (id) {
            case LOADER_ID_LOAD_HISTORY:
                loader = new HistoryLoader(getContext(), HistoryLoader.Operation.Load);
                break;
            case LOADER_ID_REMOVE_HISTORY:
                loader = new HistoryLoader(getContext(), HistoryLoader.Operation.Remove);
                break;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mHistoryTableLayout.setVisibility(View.GONE);
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<HistoryData>> loader, @NonNull final List<HistoryData> historyDataList) {
        mProgressBar.setVisibility(View.GONE);
        mHistoryTableLayout.setVisibility(View.VISIBLE);
        mHistoryDataList = historyDataList;
        refreshHistoryTable();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<HistoryData>> loader) {
    }

    @Override
    public void onSimpleDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        AppLog.vMethodIn();
        if (DIALOG_REQ_CODE_DELETE == requestCode) {
            switch (resultCode) {
                case DialogInterface.BUTTON_POSITIVE:
                    LoaderManager.getInstance(this).restartLoader(LOADER_ID_REMOVE_HISTORY, null, this);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onSimpleDialogCancelled(int requestCode, Bundle params) {
        AppLog.vMethodIn();
    }

    @NonNull
    @Override
    protected String onGetTitle() {
        return getString(history).toUpperCase();
    }

    private void refreshHistoryTable() {

        mTableLayout.removeAllViews();

        // add header
        TableRow header = (TableRow) View.inflate(getActivity(), R.layout.table_header_history, null);
        mTableLayout.addView(header);

        // sort rows
        Collections.sort(mHistoryDataList, new ResultDataComparator());

        // add rows
        int numberValue = mHistoryDataList.size();
        for (final HistoryData historyData : mHistoryDataList) {
            TableRow tableRow = (TableRow) View.inflate(getActivity(), R.layout.table_row_history, null);
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onFragmentEvent(Event.Selected, Bundler.bundle(
                            EventArg.ResultData.name(), historyData));
                }
            });
            TextView number = (TextView) tableRow.findViewById(R.id.sequenceNumber);
            TextView receivedTime = (TextView) tableRow.findViewById(R.id.received_time);
            TextView userName = (TextView) tableRow.findViewById(R.id.user_name);
            TextView type = (TextView) tableRow.findViewById(R.id.type);
            TextView protocol = (TextView) tableRow.findViewById(R.id.protocol);
            TextView modelName = (TextView) tableRow.findViewById(R.id.model_name);
            TextView localName = (TextView) tableRow.findViewById(R.id.localName);
            TextView result = (TextView) tableRow.findViewById(R.id.result);
            number.setText(StringEx.toNumberString(new BigDecimal(numberValue)));
            receivedTime.setText(StringEx.toDateString(historyData.getReceivedDate(), StringEx.FormatType.Form1));
            userName.setText(historyData.getUserName());
            type.setText(historyData.getComType().name());
            protocol.setText(historyData.getProtocol().name());
            modelName.setText(historyData.getModelName());
            localName.setText(historyData.getLocalName());
            result.setText(getString(historyData.getResultType().stringResId()));
            mTableLayout.addView(tableRow);
            numberValue--;
        }
    }

    public enum Event {
        Selected,
    }

    public enum EventArg {
        ResultData,
    }

    public interface EventListener {
        void onFragmentEvent(@NonNull Event event, Bundle args);
    }

    private static class HistoryLoader extends AsyncTaskLoader<List<HistoryData>> {

        private Operation mOperation;
        private boolean mCancelRequest;
        public HistoryLoader(@NonNull Context context, Operation operation) {
            super(context);
            mOperation = operation;
            mCancelRequest = false;
        }

        @Override
        @NonNull
        public List<HistoryData> loadInBackground() {
            AppLog.vMethodIn();
            if (Operation.Remove == mOperation) {
                HistoryManager.sharedInstance().removeAll(getContext());
            }
            AppLog.vMethodOut();
            return HistoryManager.sharedInstance().getAll(getContext());
        }

        @Override
        public void onCanceled(List<HistoryData> data) {
            super.onCanceled(data);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            mCancelRequest = true;
            cancelLoad();
        }

        @Override
        protected void onReset() {
            super.onReset();
            onStopLoading();
        }

        public enum Operation {
            Load, Remove
        }
    }

    private static class ResultDataComparator implements Comparator<HistoryData> {
        @Override
        public int compare(HistoryData data1, HistoryData data2) {
            return data1.getReceivedDate() > data2.getReceivedDate() ? -1 : 1;
        }
    }
}
