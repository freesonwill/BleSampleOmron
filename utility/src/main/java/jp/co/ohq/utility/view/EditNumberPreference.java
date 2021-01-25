package jp.co.ohq.utility.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * SharedPreferenceの読み書きをInt型で行う
 */
public class EditNumberPreference extends BaseEditPreference {

    public EditNumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditNumberPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 標準のEditTextPreferenceはString型でSharedPreferenceの読み書きを行うので、
     * 読み書き処理をオーバーロードしてInt型で読み書きする
     */
    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(0));
    }

    /**
     * 標準のEditTextPreferenceはString型でSharedPreferenceの読み書きを行うので、
     * 読み書き処理をオーバーロードしてInt型で読み書きする
     */
    @Override
    protected boolean persistString(String value) {
        boolean ret = false;
        if (null != value && !value.isEmpty()) {
            ret = persistInt(Integer.valueOf(value));
        }
        return ret;
    }
}