package jp.co.ohq.utility.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

import jp.co.ohq.utility.R;

abstract class BaseEditPreference extends EditTextPreference {

    private int mMinLength;

    public BaseEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    public BaseEditPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BaseEditPreference, 0, 0);
        mMinLength = a.getInt(R.styleable.BaseEditPreference_minLength, Integer.MAX_VALUE);
        a.recycle();
    }

//    @Override
//    protected View onCreateView(ViewGroup parent) {
//        super.onCreateView(parent);
//        final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = layoutInflater.inflate(R.layout.preference_base_edit, parent, false);
//        return view;
//    }
//
//    @Override
//    protected void onBindView(View view) {
//        super.onBindView(view);
//        mValueView = (TextView) view.findViewById(R.id.value);
//        String value = getPersistedString(null);
//        if (null != value) {
//            setValue(value);
//        }
//    }

//    @Override
//    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
//        super.onAddEditTextToDialogView(dialogView, editText);
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                updateOKButton(s.length());
//            }
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
//    }

//    @Override
//    protected void showDialog(Bundle state) {
//        super.showDialog(state);
//        updateOKButton(getText().length());
//    }
//
//    private void updateOKButton(int textLength) {
//        Dialog dialog = getDialog();
//        if (dialog != null) {
//            Button okButton = (Button) dialog.findViewById(android.R.id.button1);
//            okButton.setEnabled(mMinLength <= textLength);
//        }
//    }
//
//    @Override
//    protected boolean callChangeListener(Object newValue) {
//        String value = (String) newValue;
//        mValueView.setText(value);
//        return super.callChangeListener(newValue);
//    }
//
//    public String getValue() {
//        return mValueView.getText().toString();
//    }
//
//    public void setValue(String value) {
//        mValueView.setText(value);
//        setText(value);
//    }
}