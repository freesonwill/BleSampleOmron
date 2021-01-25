//
//  State.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.utility.sm;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class State implements IState {

    static final boolean HANDLED = true;
    static final boolean NOT_HANDLED = false;

    @Override
    public void enter(@Nullable Object[] transferObjects) {
    }

    @Override
    public boolean processMessage(@NonNull Message msg) {
        return NOT_HANDLED;
    }

    @Override
    public void exit() {
    }

    @Override
    @NonNull
    public String getName() {
        String name = getClass().getName();
        int lastDollar = name.lastIndexOf('$');
        return name.substring(lastDollar + 1);
    }
}
