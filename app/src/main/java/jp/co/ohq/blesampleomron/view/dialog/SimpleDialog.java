package jp.co.ohq.blesampleomron.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;

public final class SimpleDialog extends DialogFragment {

    private Callback mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            mCallback = (Callback) context;
        } else if (getTargetFragment() instanceof Callback) {
            mCallback = (Callback) getTargetFragment();
        } else {
            throw new AndroidRuntimeException("Parent is must be implement 'Callback'");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                mCallback.onSimpleDialogSucceeded(getRequestCode(), which, getArguments().getBundle("params"));
            }
        };
        final String title = getArguments().getString("title");
        final String message = getArguments().getString("message");
        final String[] items = getArguments().getStringArray("items");
        final String positiveLabel = getArguments().getString("positive_label");
        final String negativeLabel = getArguments().getString("negative_label");
        setCancelable(getArguments().getBoolean("cancelable"));
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }
        if (items != null && items.length > 0) {
            builder.setItems(items, listener);
        }
        if (!TextUtils.isEmpty(positiveLabel)) {
            builder.setPositiveButton(positiveLabel, listener);
        }
        if (!TextUtils.isEmpty(negativeLabel)) {
            builder.setNegativeButton(negativeLabel, listener);
        }
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mCallback.onSimpleDialogCancelled(getRequestCode(), getArguments().getBundle("params"));
    }

    private int getRequestCode() {
        return getArguments().containsKey("request_code") ? getArguments().getInt("request_code") : getTargetRequestCode();
    }

    public interface Callback {

        /**
         * @param requestCode Request code
         * @param resultCode  DialogInterface.BUTTON_(POSI|NEGA)TIVE or list position
         * @param params      User parameters
         */
        void onSimpleDialogSucceeded(int requestCode, int resultCode, Bundle params);

        /**
         * @param requestCode Request code
         * @param params      User parameters
         */
        void onSimpleDialogCancelled(int requestCode, Bundle params);
    }

    public static class Builder {
        final AppCompatActivity mActivity;
        final Fragment mParentFragment;
        String mTitle;
        String mMessage;
        String[] mItems;
        String mPositiveLabel;
        String mNegativeLabel;
        int mRequestCode = -1;
        Bundle mParams;
        String mTag = "default";
        boolean mCancelable = true;

        /**
         * Constructor for activity
         */
        public <A extends AppCompatActivity & Callback> Builder(@NonNull final A activity) {
            mActivity = activity;
            mParentFragment = null;
        }

        /**
         * Constructor for fragment
         */
        public <F extends Fragment & Callback> Builder(@NonNull final F parentFragment) {
            mParentFragment = parentFragment;
            mActivity = null;
        }

        public Builder title(@NonNull final String title) {
            mTitle = title;
            return this;
        }

        public Builder title(@StringRes final int title) {
            return title(getContext().getString(title));
        }

        public Builder message(@NonNull final String message) {
            mMessage = message;
            return this;
        }

        public Builder message(@StringRes final int message) {
            return message(getContext().getString(message));
        }

        public Builder items(@NonNull final String... items) {
            mItems = items;
            return this;
        }

        public Builder positive(@NonNull final String positiveLabel) {
            mPositiveLabel = positiveLabel;
            return this;
        }

        public Builder positive(@StringRes final int positiveLabel) {
            return positive(getContext().getString(positiveLabel));
        }

        public Builder negative(@NonNull final String negativeLabel) {
            mNegativeLabel = negativeLabel;
            return this;
        }

        public Builder negative(@StringRes final int negativeLabel) {
            return negative(getContext().getString(negativeLabel));
        }

        public Builder requestCode(final int requestCode) {
            mRequestCode = requestCode;
            return this;
        }

        public Builder tag(final String tag) {
            mTag = tag;
            return this;
        }

        public Builder params(final Bundle params) {
            mParams = new Bundle(params);
            return this;
        }

        public Builder cancelable(final boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public void show() {
            final Bundle args = new Bundle();
            args.putString("title", mTitle);
            args.putString("message", mMessage);
            args.putStringArray("items", mItems);
            args.putString("positive_label", mPositiveLabel);
            args.putString("negative_label", mNegativeLabel);
            args.putBoolean("cancelable", mCancelable);
            if (mParams != null) {
                args.putBundle("params", mParams);
            }

            final SimpleDialog f = new SimpleDialog();
            if (mParentFragment != null) {
                f.setTargetFragment(mParentFragment, mRequestCode);
            } else {
                args.putInt("request_code", mRequestCode);
            }
            f.setArguments(args);
            if (mParentFragment != null) {
                f.show(mParentFragment.getFragmentManager(), mTag);
            } else {
                f.show(mActivity.getSupportFragmentManager(), mTag);
            }
        }

        private Context getContext() {
            return (mActivity == null ? mParentFragment.getActivity() : mActivity).getApplicationContext();
        }
    }
}
