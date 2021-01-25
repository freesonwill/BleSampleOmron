package jp.co.ohq.blesampleomron.model.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogViewService extends Service {

    private static final int LOG_LIST_LINE_MAX = 30000;
    private static final int GET_LOG_LINE_MAX = 3000;
    private final List<String> mLogList = new ArrayList<>();
    private final Object mSync = new Object();
    private Thread mThread = null;
    private LogLevel mLogLevel = LogLevel.Verbose;
    private Req mReq = Req.None;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (mThread != null) {
            return START_NOT_STICKY;
        }

        mThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    synchronized (mSync) {
                        mReq = Req.None;
                    }
                    Process process = null;
                    BufferedReader reader = null;
                    try {
                        process = Runtime.getRuntime().exec(new String[]{"logcat", "-v", "time", mLogLevel.getCommand()});
                        reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"), 1024);
                        String line;
                        while (true) {
                            synchronized (mSync) {
                                if (mReq == Req.StopThread || mReq == Req.RestartLogcat) {
                                    break;
                                }
                            }
                            if (reader.ready()) {
                                line = reader.readLine();
                            } else {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }

                            if (line == null || line.length() == 0) {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }

                            synchronized (mSync) {
                                if (mLogList.size() >= LOG_LIST_LINE_MAX) {
                                    mLogList.remove(0);
                                }
                                mLogList.add(line);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (null != process) {
                            process.destroy();
                        }
                    }

                    if (mReq == Req.StopThread) {
                        mReq = Req.None;
                        mThread = null;
                        break;
                    }
                }
            }
        });

        mThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public List<String> getLog() {
        ArrayList<String> ret;
        synchronized (mSync) {
            int logListSize = mLogList.size();
            if (GET_LOG_LINE_MAX > logListSize) {
                ret = new ArrayList<>(mLogList);
            } else {
                int start = logListSize - GET_LOG_LINE_MAX;
                ret = new ArrayList<>(mLogList.subList(start, logListSize));
            }
        }
        return ret;
    }

    public void clearLog() {
        synchronized (mSync) {
            mLogList.clear();
        }
    }

    public LogLevel getLogLevel() {
        synchronized (mSync) {
            return mLogLevel;
        }
    }

    public void setLogLevel(LogLevel logLevel) {
        synchronized (mSync) {
            mLogList.clear();
            mLogLevel = logLevel;
            mReq = Req.RestartLogcat;
        }
    }

    public void setLogBreak() {
        synchronized (mSync) {
            mReq = Req.StopThread;
        }
    }

    public enum LogLevel {
        Verbose("*:V"), Debug("*:D"), Info("*:I"), Warning("*:W"), Error("*:E");
        private String command;

        LogLevel(String command) {
            this.command = command;
        }

        public String getCommand() {
            return this.command;
        }
    }

    private enum Req {
        None,
        StopThread,
        RestartLogcat
    }

    public class LocalBinder extends Binder {
        public LogViewService getService() {
            return LogViewService.this;
        }
    }
}
