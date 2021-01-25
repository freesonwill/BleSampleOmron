package jp.co.ohq.utility;

import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SynchronizeCallback {
    private final static int DEFAULT_TIMEOUT = 10 * 1000;
    @NonNull
    private final CountDownLatch mLock;
    private Object mResult;

    public SynchronizeCallback() {
        mLock = new CountDownLatch(1);
    }

    public void lock() {
        try {
            lock(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void lock(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        mLock.await(timeout, unit);
        if (0 < mLock.getCount()) {
            throw new TimeoutException("CountDownLatch.await() is timeout.");
        }
    }

    public void unlock() {
        mLock.countDown();
    }

    public Object getResult() {
        return mResult;
    }

    public void setResult(Object result) {
        mResult = result;
    }
}
