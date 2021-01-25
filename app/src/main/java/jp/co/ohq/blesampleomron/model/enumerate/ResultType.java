package jp.co.ohq.blesampleomron.model.enumerate;

import android.support.annotation.StringRes;

import jp.co.ohq.blesampleomron.R;

public enum ResultType {
    Success(R.string.success),
    Failure(R.string.failure);
    @StringRes
    int id;

    ResultType(@StringRes int id) {
        this.id = id;
    }

    @StringRes
    public int stringResId() {
        return this.id;
    }
}
