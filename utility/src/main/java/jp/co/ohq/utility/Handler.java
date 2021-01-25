package jp.co.ohq.utility;

import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

public class Handler extends android.os.Handler {

    public Handler() {
        super();
    }

    public Handler(@Nullable Looper looper) {
        super(looper);
    }

    public final Message obtainMessage(int what, int arg1) {
        return Message.obtain(this, what, arg1, 0);
    }

    public final void sendMessage(int what) {
        sendMessage(obtainMessage(what));
    }

    public final void sendMessage(int what, Object obj) {
        sendMessage(obtainMessage(what, obj));
    }

    public final void sendMessage(int what, int arg1) {
        sendMessage(obtainMessage(what, arg1));
    }

    public final void sendMessage(int what, int arg1, int arg2) {
        sendMessage(obtainMessage(what, arg1, arg2));
    }

    public final void sendMessage(int what, int arg1, int arg2, Object obj) {
        sendMessage(obtainMessage(what, arg1, arg2, obj));
    }

    public final void sendMessageDelayed(int what, long delayMillis) {
        sendMessageDelayed(obtainMessage(what), delayMillis);
    }

    public final void sendMessageDelayed(int what, Object obj, long delayMillis) {
        sendMessageDelayed(obtainMessage(what, obj), delayMillis);
    }

    public final void sendMessageDelayed(int what, int arg1, long delayMillis) {
        sendMessageDelayed(obtainMessage(what, arg1), delayMillis);
    }

    public final void sendMessageDelayed(int what, int arg1, int arg2, long delayMillis) {
        sendMessageDelayed(obtainMessage(what, arg1, arg2), delayMillis);
    }

    public final void sendMessageDelayed(int what, int arg1, int arg2, Object obj, long delayMillis) {
        sendMessageDelayed(obtainMessage(what, arg1, arg2, obj), delayMillis);
    }

    public final void sendMessageAtFrontOfQueue(int what) {
        sendMessageAtFrontOfQueue(obtainMessage(what));
    }

    public final void sendMessageAtFrontOfQueue(int what, Object obj) {
        sendMessageAtFrontOfQueue(obtainMessage(what, obj));
    }

    public final void sendMessageAtFrontOfQueue(int what, int arg1) {
        sendMessageAtFrontOfQueue(obtainMessage(what, arg1));
    }

    public final void sendMessageAtFrontOfQueue(int what, int arg1, int arg2) {
        sendMessageAtFrontOfQueue(obtainMessage(what, arg1, arg2));
    }

    public final void sendMessageAtFrontOfQueue(int what, int arg1, int arg2, Object obj) {
        sendMessageAtFrontOfQueue(obtainMessage(what, arg1, arg2, obj));
    }

    public final boolean isCurrentThread() {
        return Thread.currentThread() == getLooper().getThread();
    }
}
