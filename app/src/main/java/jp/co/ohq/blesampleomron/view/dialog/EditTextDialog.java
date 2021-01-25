package jp.co.ohq.blesampleomron.view.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AndroidRuntimeException;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import jp.co.ohq.blesampleomron.R;

public final class EditTextDialog extends DialogFragment implements TextWatcher {

    private Callback mCallback;
    private AlertDialog mAlertDialog;
    private EditText mEditText;
    private String mTitle;
    private String mInitValue;

    public static EditTextDialog newInstance(String title, String initValue) {
        EditTextDialog fragment = new EditTextDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("initValue", initValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            mCallback = (Callback) context;
        } else {
            if (getParentFragment() instanceof Callback) {
                mCallback = (Callback) getParentFragment();
            } else {
                throw new AndroidRuntimeException("Parent is must be implement 'Callback'");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTitle = args.getString("title", null);
        mInitValue = args.getString("initValue", null);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (null != mTitle) {
            builder.setTitle(mTitle);
        }
        builder.setView(createDialogView(mInitValue))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (DialogInterface.BUTTON_POSITIVE == which) {
                            mCallback.onEditTextDialogEdited(mEditText.getText().toString());
                        } else {
                            mCallback.onEditTextDialogCanceled();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return mAlertDialog;
    }

    private View createDialogView(String initValue) {
        View view = View.inflate(getActivity(), R.layout.dialog_edit_text, null);
        mEditText = (EditText) view.findViewById(R.id.editText);
        mEditText.setText(initValue);
        mEditText.addTextChangedListener(this);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mCallback.onEditTextDialogEdited(v.getText().toString());
                    mAlertDialog.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAlertDialog = null;
        mEditText = null;
    }

    /* Not used */
    public void afterTextChanged(Editable s) {
    }

    /* Not used */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /* Not used */
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public interface Callback {
        void onEditTextDialogEdited(String resultCode);

        void onEditTextDialogCanceled();
    }
}
