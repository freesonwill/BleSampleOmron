package jp.co.ohq.blesampleomron.model.system;

import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jp.co.ohq.blesampleomron.controller.util.AppLog;
import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.SynchronizeCallback;
import jp.co.ohq.utility.Types;

public final class LoggingManager {

    private final static long BUFFERING_INTERVAL = 200;
    private final static long STOPPING_TIMEOUT = 2 * 1000;
    private final static String START_WORD_PREFIX = "+++===+++===+++===+++===+++=== LOGGING START +++===+++===+++===+++===+++===";
    private final static String STOP_WORD_PREFIX = "===+++===+++===+++===+++===+++ LOGGING STOP ===+++===+++===+++===+++===+++";

    @NonNull
    private final List<String> mLogBuffer = new ArrayList<>();
    @NonNull
    private final Handler mHandler;
    @Nullable
    private Process mProcess;
    @Nullable
    private BufferedReader mReader;
    @NonNull
    private String mStartWord = "";
    @NonNull
    private String mStopWord = "";
    @Nullable
    private ActionListener mActionListener;
    @Nullable
    private Thread mLoggingThread;
    @NonNull
    private final Runnable mLoggingRunnable = new Runnable() {
        @Override
        public void run() {
            if (null == mReader) {
                throw new AndroidRuntimeException("null == mReader");
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    _onLoggingStarted();
                }
            });
            try {
                boolean isStartWordFound = false;
                String line;
                while (true) {
                    if (mReader.ready()) {
                        line = mReader.readLine();
                    } else {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    if (TextUtils.isEmpty(line)) {
                        Thread.sleep(BUFFERING_INTERVAL);
                    } else {
                        if (isStartWordFound) {
                            mLogBuffer.add(line);
                        } else {
                            if (line.contains(mStartWord)) {
                                isStartWordFound = true;
                                mLogBuffer.clear();
                                mLogBuffer.add(line);
                            }
                        }
                        if (line.contains(mStopWord)) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    _onLoggingStopped();
                }
            });
        }
    };
    @NonNull
    private final Runnable mForceStopRunnable = new Runnable() {
        @Override
        public void run() {
            _forceStop();
        }
    };

    public LoggingManager() {
        HandlerThread thread = new HandlerThread("LoggingManager");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    public void start(@NonNull ActionListener actionListener) {
        start(LogLevel.Verbose, actionListener);
    }

    public void start(@NonNull final LogLevel logLevel, @NonNull final ActionListener actionListener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _start(logLevel, actionListener);
            }
        });
    }

    public void stop(@NonNull final ActionListener actionListener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                _stop(actionListener);
            }
        });
    }

    @NonNull
    public List<String> getLastLog() {
        final List<String> ret;
        final SynchronizeCallback callback = new SynchronizeCallback();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.setResult(mLogBuffer);
                callback.unlock();
            }
        });
        callback.lock();
        ret = Types.autoCast(callback.getResult());
        return ret;
    }

    private void _start(@NonNull LogLevel logLevel, @NonNull ActionListener actionListener) {
        AppLog.vMethodIn(logLevel.name());

        if (null != mActionListener) {
            AppLog.e("busy.");
            actionListener.onFailure();
            return;
        }

        if (null != mReader) {
            AppLog.e("null != mReader");
            actionListener.onFailure();
            return;
        }

        try {
            mProcess = Runtime.getRuntime().exec(new String[]{"logcat", "-v", "time", logLevel.getCommand()});
        } catch (IOException e) {
            e.printStackTrace();
            actionListener.onFailure();
            return;
        }

        try {
            mReader = new BufferedReader(
                    new InputStreamReader(mProcess.getInputStream(), "UTF-8"), 1024);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            mProcess.destroy();
            actionListener.onFailure();
            return;
        }

        mActionListener = actionListener;

        mLoggingThread = new Thread(mLoggingRunnable);
        mStartWord = START_WORD_PREFIX + " " + mLoggingThread.hashCode();
        mStopWord = STOP_WORD_PREFIX + " " + mLoggingThread.hashCode();

        AppLog.i(mStartWord);
        mLoggingThread.start();
    }

    private void _stop(@NonNull ActionListener actionListener) {
        AppLog.vMethodIn();

        if (null != mActionListener) {
            AppLog.e("busy.");
            actionListener.onFailure();
            return;
        }

        if (null == mReader) {
            AppLog.e("null == mReader");
            actionListener.onFailure();
            return;
        }

        mActionListener = actionListener;

        mHandler.postDelayed(mForceStopRunnable, STOPPING_TIMEOUT);

        AppLog.i(mStopWord);
    }

    private void _forceStop() {
        AppLog.vMethodIn();

        if (null == mReader) {
            throw new AndroidRuntimeException("null == mReader");
        }
        if (null != mProcess) {
            mProcess.destroy();
            mProcess = null;
        }
        try {
            mReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mReader = null;
        if (null != mLoggingThread) {
            mLoggingThread.interrupt();
        }
    }

    private void _onLoggingStarted() {
        AppLog.vMethodIn();

        if (null == mActionListener) {
            throw new AndroidRuntimeException("null == mActionListener");
        }
        ActionListener listener = mActionListener;
        mActionListener = null;

        listener.onSuccess();
    }

    private void _onLoggingStopped() {
        AppLog.vMethodIn();

        mHandler.removeCallbacks(mForceStopRunnable);

        if (null != mProcess) {
            mProcess.destroy();
            mProcess = null;
        }

        if (null != mReader) {
            try {
                mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mReader = null;
        }

        mLoggingThread = null;

        if (null == mActionListener) {
            throw new AndroidRuntimeException("null == mActionListener");
        }
        ActionListener listener = mActionListener;
        mActionListener = null;

        listener.onSuccess();
    }

    private enum LogLevel {
        Verbose("*:V"),
        Debug("*:D"),
        Info("*:I"),
        Warning("*:W"),
        Error("*:E"),;
        private String command;

        LogLevel(String command) {
            this.command = command;
        }

        String getCommand() {
            return this.command;
        }
    }

    public interface ActionListener {
        void onSuccess();

        void onFailure();
    }
}
