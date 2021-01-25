//
//  StateMachine.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.utility.sm;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.log.UtilLog;

public abstract class StateMachine {

    protected static final boolean HANDLED = State.HANDLED;
    protected static final boolean NOT_HANDLED = State.NOT_HANDLED;

    private String mName = StateMachine.class.getSimpleName();
    private String mTag = StateMachine.class.getSimpleName();
    private boolean mDbg;
    private SmHandler mSmHandler;
    private HandlerThread mSmThread;

    public StateMachine(@Nullable String name) {
        initStateMachine(name, null);
    }

    public StateMachine(@Nullable String name, @NonNull Handler handler) {
        initStateMachine(name, handler.getLooper());
    }

    public StateMachine(@Nullable String name, @Nullable Looper looper) {
        initStateMachine(name, looper);
    }

    private void initStateMachine(@Nullable String name, @Nullable Looper looper) {
        if (null == name) {
            name = this.getClass().getSimpleName();
        }
        if (null == looper) {
            mSmThread = new HandlerThread(name);
            mSmThread.start();
            looper = mSmThread.getLooper();
        }
        mName = name;
        mSmHandler = new SmHandler(looper, this);
    }

    protected final void addState(State state, State parent) {
        mSmHandler.addState(state, parent);
    }

    protected final void addState(State state) {
        mSmHandler.addState(state, null);
    }

    protected final void setInitialState(State initialState) {
        mSmHandler.setInitialState(initialState);
    }

    protected final Message getCurrentMessage() {
        return mSmHandler.getCurrentMessage();
    }

    protected final State getCurrentState() {
        return mSmHandler.getCurrentState();
    }

    protected final void transitionTo(State destState) {
        mSmHandler.transitionTo(destState, null);
    }

    protected final void transitionTo(State destState, Object[] objects) {
        mSmHandler.transitionTo(destState, objects);
    }

    private Message obtainMessage() {
        return Message.obtain(mSmHandler);
    }

    private Message obtainMessage(int what) {
        return Message.obtain(mSmHandler, what);
    }

    private Message obtainMessage(int what, Object obj) {
        return Message.obtain(mSmHandler, what, obj);
    }

    private Message obtainMessage(int what, int arg1) {
        // use this obtain so we don't match the obtain(h, what, Object) method
        return Message.obtain(mSmHandler, what, arg1, 0);
    }

    private Message obtainMessage(int what, int arg1, int arg2) {
        return Message.obtain(mSmHandler, what, arg1, arg2);
    }

    private Message obtainMessage(int what, int arg1, int arg2, Object obj) {
        return Message.obtain(mSmHandler, what, arg1, arg2, obj);
    }

    public final void sendMessage(int what) {
        mSmHandler.sendMessage(obtainMessage(what));
    }

    public final void sendMessage(int what, Object obj) {
        mSmHandler.sendMessage(obtainMessage(what, obj));
    }

    public final void sendMessage(int what, int arg1) {
        mSmHandler.sendMessage(obtainMessage(what, arg1));
    }

    public final void sendMessage(int what, int arg1, int arg2) {
        mSmHandler.sendMessage(obtainMessage(what, arg1, arg2));
    }

    public final void sendMessage(int what, int arg1, int arg2, Object obj) {
        mSmHandler.sendMessage(obtainMessage(what, arg1, arg2, obj));
    }

    public final void sendMessage(Message msg) {
        mSmHandler.sendMessage(msg);
    }

    public final void sendMessageDelayed(int what, long delayMillis) {
        mSmHandler.sendMessageDelayed(obtainMessage(what), delayMillis);
    }

    public final void sendMessageDelayed(int what, Object obj, long delayMillis) {
        mSmHandler.sendMessageDelayed(obtainMessage(what, obj), delayMillis);
    }

    public final void sendMessageDelayed(int what, int arg1, long delayMillis) {
        mSmHandler.sendMessageDelayed(obtainMessage(what, arg1), delayMillis);
    }

    public final void sendMessageDelayed(int what, int arg1, int arg2, long delayMillis) {
        mSmHandler.sendMessageDelayed(obtainMessage(what, arg1, arg2), delayMillis);
    }

    public final void sendMessageDelayed(int what, int arg1, int arg2, Object obj, long delayMillis) {
        mSmHandler.sendMessageDelayed(obtainMessage(what, arg1, arg2, obj), delayMillis);
    }

    public final void sendMessageDelayed(Message msg, long delayMillis) {
        mSmHandler.sendMessageDelayed(msg, delayMillis);
    }

    public final void sendMessageAtFrontOfQueue(int what) {
        mSmHandler.sendMessageAtFrontOfQueue(obtainMessage(what));
    }

    public final void sendMessageAtFrontOfQueue(int what, Object obj) {
        mSmHandler.sendMessageAtFrontOfQueue(obtainMessage(what, obj));
    }

    public final void sendMessageAtFrontOfQueue(int what, int arg1) {
        mSmHandler.sendMessageAtFrontOfQueue(obtainMessage(what, arg1));
    }

    public final void sendMessageAtFrontOfQueue(int what, int arg1, int arg2) {
        mSmHandler.sendMessageAtFrontOfQueue(obtainMessage(what, arg1, arg2));
    }

    public final void sendMessageAtFrontOfQueue(int what, int arg1, int arg2, Object obj) {
        mSmHandler.sendMessageAtFrontOfQueue(obtainMessage(what, arg1, arg2, obj));
    }

    public final void sendMessageAtFrontOfQueue(Message msg) {
        mSmHandler.sendMessageAtFrontOfQueue(msg);
    }

    public final boolean hasMessage(int what) {
        return mSmHandler.hasMessages(what);
    }

    public final void removeMessages(int what) {
        mSmHandler.removeMessages(what);
    }

    protected final void deferMessage(Message msg) {
        mSmHandler.deferMessage(msg);
    }

    protected final void removeDeferredMessages(int what) {
        Iterator<Message> iterator = mSmHandler.mDeferredMessages.iterator();
        while (iterator.hasNext()) {
            Message msg = iterator.next();
            if (msg.what == what) iterator.remove();
        }
    }

    public void start() {
        mSmHandler.completeConstruction();
    }

    public final void quit() {
        mSmHandler.quit();
    }

    public final void quitNow() {
        mSmHandler.quitNow();
    }

    public final String getName() {
        return mName;
    }

    public final Handler getHandler() {
        return mSmHandler;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public void setDbg(boolean dbg) {
        mDbg = dbg;
    }

    private void log(String s) {
        if (mDbg) {
            UtilLog.d(mTag, s);
        }
    }

    private static class SmHandler extends Handler {

        private static final int SM_QUIT_CMD = -1;
        private static final int SM_INIT_CMD = -2;
        private static final Object mSmHandlerObj = new Object();
        private boolean mHasQuit = false;
        private Message mMsg;
        private Object[] mTransferObjects;
        private boolean mIsConstructionCompleted;
        private StateInfo mStateStack[];
        private int mStateStackTopIndex = -1;
        private StateInfo mTempStateStack[];
        private int mTempStateStackCount;
        private QuittingState mQuittingState = new QuittingState();
        private StateMachine mSm;
        private HashMap<State, StateInfo> mStateInfo = new HashMap<State, StateInfo>();
        private State mInitialState;
        private State mDestState;
        private ArrayList<Message> mDeferredMessages = new ArrayList<Message>();

        private SmHandler(Looper looper, StateMachine sm) {
            super(looper);
            mSm = sm;

            addState(mQuittingState, null);
        }

        @Override
        public final void handleMessage(Message msg) {
            if (!mHasQuit) {
                mMsg = msg;

                State msgProcessedState = null;
                if (mIsConstructionCompleted) {
                    msgProcessedState = processMsg(msg);
                } else if (mMsg.what == SM_INIT_CMD && (mMsg.obj == mSmHandlerObj)) {
                    mIsConstructionCompleted = true;
                    invokeEnterMethods(0);
                } else {
                    throw new RuntimeException("StateMachine.handleMessage: "
                            + "The start method not called, received msg: " + msg);
                }
                performTransitions(msgProcessedState, msg);
            }
        }

        private void performTransitions(State msgProcessedState, Message msg) {

            State destState = mDestState;
            if (destState != null) {
                while (true) {
                    StateInfo commonStateInfo = setupTempStateStackWithStatesToEnter(destState);
                    invokeExitMethods(commonStateInfo);
                    int stateStackEnteringIndex = moveTempStateStackToStateStack();
                    invokeEnterMethods(stateStackEnteringIndex);
                    moveDeferredMessageAtFrontOfQueue();

                    if (destState != mDestState) {
                        destState = mDestState;
                    } else {
                        break;
                    }
                }
                mDestState = null;
            }

            if (destState != null) {
                if (destState == mQuittingState) {
                    cleanupAfterQuitting();
                }
            }
        }

        private final void cleanupAfterQuitting() {
            if (mSm.mSmThread != null) {
                getLooper().quit();
                mSm.mSmThread = null;
            }

            mSm.mSmHandler = null;
            mSm = null;
            mMsg = null;
            mStateStack = null;
            mTempStateStack = null;
            mStateInfo.clear();
            mInitialState = null;
            mDestState = null;
            mDeferredMessages.clear();
            mHasQuit = true;
        }

        private final void completeConstruction() {
            int maxDepth = 0;
            for (StateInfo si : mStateInfo.values()) {
                int depth = 0;
                for (StateInfo i = si; i != null; depth++) {
                    i = i.parentStateInfo;
                }
                if (maxDepth < depth) {
                    maxDepth = depth;
                }
            }
            mStateStack = new StateInfo[maxDepth];
            mTempStateStack = new StateInfo[maxDepth];
            setupInitialStateStack();

            sendMessageAtFrontOfQueue(obtainMessage(SM_INIT_CMD, mSmHandlerObj));
        }

        private final State processMsg(Message msg) {
            StateInfo curStateInfo = mStateStack[mStateStackTopIndex];

            mSm.log("processMsg: " + curStateInfo.state.getName() + String.format(Locale.US, " msg.what=0x%08x", msg.what));

            if (isQuit(msg)) {
                transitionTo(mQuittingState, null);
            } else {
                while (!curStateInfo.state.processMessage(msg)) {
                    curStateInfo = curStateInfo.parentStateInfo;
                    if (curStateInfo == null) {
                        break;
                    }
                    mSm.log("processMsg: " + curStateInfo.state.getName() + String.format(Locale.US, " msg.what=0x%08x", msg.what));
                }
            }
            return (curStateInfo != null) ? curStateInfo.state : null;
        }

        private final void invokeExitMethods(StateInfo commonStateInfo) {
            while ((mStateStackTopIndex >= 0)
                    && (mStateStack[mStateStackTopIndex] != commonStateInfo)) {
                State curState = mStateStack[mStateStackTopIndex].state;
                mSm.log("invokeExitMethods: " + curState.getName());
                curState.exit();
                mStateStack[mStateStackTopIndex].active = false;
                mStateStackTopIndex -= 1;
            }
        }

        private final void invokeEnterMethods(int stateStackEnteringIndex) {
            final Object[] transferObjects = mTransferObjects;
            mTransferObjects = null;
            for (int i = stateStackEnteringIndex; i <= mStateStackTopIndex; i++) {
                mSm.log("invokeEnterMethods: " + mStateStack[i].state.getName());
                mStateStack[i].state.enter(transferObjects);
                mStateStack[i].active = true;
            }
        }

        private final void moveDeferredMessageAtFrontOfQueue() {
            for (int i = mDeferredMessages.size() - 1; i >= 0; i--) {
                Message curMsg = mDeferredMessages.get(i);
                mSm.log("moveDeferredMessageAtFrontOfQueue; " + String.format(Locale.US, "msg.what=0x%08x", curMsg.what));
                sendMessageAtFrontOfQueue(curMsg);
            }
            mDeferredMessages.clear();
        }

        private final int moveTempStateStackToStateStack() {
            int startingIndex = mStateStackTopIndex + 1;
            int i = mTempStateStackCount - 1;
            int j = startingIndex;
            while (i >= 0) {
                mStateStack[j] = mTempStateStack[i];
                j += 1;
                i -= 1;
            }
            mStateStackTopIndex = j - 1;
            return startingIndex;
        }

        private final StateInfo setupTempStateStackWithStatesToEnter(State destState) {
            mTempStateStackCount = 0;
            StateInfo curStateInfo = mStateInfo.get(destState);
            do {
                mTempStateStack[mTempStateStackCount++] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
            } while ((curStateInfo != null) && !curStateInfo.active);
            return curStateInfo;
        }

        private final void setupInitialStateStack() {
            mSm.log("setupInitialStateStack: E mInitialState=" + mInitialState.getName());

            StateInfo curStateInfo = mStateInfo.get(mInitialState);
            for (mTempStateStackCount = 0; curStateInfo != null; mTempStateStackCount++) {
                mTempStateStack[mTempStateStackCount] = curStateInfo;
                curStateInfo = curStateInfo.parentStateInfo;
            }

            mStateStackTopIndex = -1;

            moveTempStateStackToStateStack();
        }

        private final Message getCurrentMessage() {
            return mMsg;
        }

        private final State getCurrentState() {
            return mStateStack[mStateStackTopIndex].state;
        }

        private final StateInfo addState(State state, State parent) {
            mSm.log("addStateInternal: E state=" + state.getName() + ",parent="
                    + ((parent == null) ? "" : parent.getName()));
            StateInfo parentStateInfo = null;
            if (parent != null) {
                parentStateInfo = mStateInfo.get(parent);
                if (parentStateInfo == null) {
                    parentStateInfo = addState(parent, null);
                }
            }
            StateInfo stateInfo = mStateInfo.get(state);
            if (stateInfo == null) {
                stateInfo = new StateInfo();
                mStateInfo.put(state, stateInfo);
            }

            if ((stateInfo.parentStateInfo != null)
                    && (stateInfo.parentStateInfo != parentStateInfo)) {
                throw new RuntimeException("state already added");
            }
            stateInfo.state = state;
            stateInfo.parentStateInfo = parentStateInfo;
            stateInfo.active = false;
            mSm.log("addStateInternal: X stateInfo: " + stateInfo);
            return stateInfo;
        }

        private final void setInitialState(State initialState) {
            mSm.log("setInitialState: initialState=" + initialState.getName());
            mInitialState = initialState;
        }

        private final void transitionTo(State destState, Object[] objects) {
            mDestState = destState;
            mTransferObjects = objects;
        }

        private final void deferMessage(Message msg) {
            mSm.log("deferMessage: " + String.format(Locale.US, "msg.what=0x%08x", msg.what));
            Message newMsg = obtainMessage();
            newMsg.copyFrom(msg);

            mDeferredMessages.add(newMsg);
        }

        private final void quit() {
            mSm.log("quit:");
            sendMessage(obtainMessage(SM_QUIT_CMD, mSmHandlerObj));
        }

        private final void quitNow() {
            mSm.log("quitNow:");
            sendMessageAtFrontOfQueue(obtainMessage(SM_QUIT_CMD, mSmHandlerObj));
        }

        private final boolean isQuit(Message msg) {
            return (msg.what == SM_QUIT_CMD) && (msg.obj == mSmHandlerObj);
        }

        private static class StateInfo {
            State state;
            StateInfo parentStateInfo;
            boolean active;

            @Override
            public String toString() {
                return "state=" + state.getName() + ",active=" + active + ",parent="
                        + ((parentStateInfo == null) ? "null" : parentStateInfo.state.getName());
            }
        }

        private static class QuittingState extends State {
            @Override
            public boolean processMessage(Message msg) {
                return NOT_HANDLED;
            }
        }
    }
}
