//
//  CBLog.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.utility.log;

public abstract class AbsLog {

    protected static final String LOG_PREFIX_DEBUG = "[DEBUG]";
    protected static final String LOG_PREFIX_INFO = "[INFO]";
    protected static final String LOG_PREFIX_WARN = "[WARN]";
    protected static final String LOG_PREFIX_ERROR = "[ERROR]";
    protected static final String LOG_PREFIX_METHOD_IN = "[IN]";
    protected static final String LOG_PREFIX_METHOD_OUT = "[OUT]";
    private static final String TAG_DEFAULT = AbsLog.class.getName();

    protected static void outputLog(String tag, LogLevel logLevel, boolean addThreadName, String msg) {
        String threadName = addThreadName ? "[" + Thread.currentThread().getName() + "-Thread]" : "";
        tag = null == tag ? TAG_DEFAULT : tag;
        logLevel = null == logLevel ? LogLevel.Error : logLevel;
        msg = threadName + msg;
        switch (logLevel) {
            case Debug:
                android.util.Log.d(tag, msg);
                break;
            case Info:
                android.util.Log.i(tag, msg);
                break;
            case Warn:
                android.util.Log.w(tag, msg);
                break;
            case Error:
                android.util.Log.e(tag, msg);
                break;
            default:
                android.util.Log.v(tag, msg);
                break;
        }
    }

    protected static String methodNameString(int stackDepth) {
        final StackTraceElement element = Thread.currentThread().getStackTrace()[stackDepth];
        final String fullClassName = element.getClassName();
        final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        final String methodName = element.getMethodName();
        final int lineNumber = element.getLineNumber();
        return simpleClassName + "#" + methodName + ":" + lineNumber;
    }

    public enum LogLevel {
        Verbose, Debug, Info, Warn, Error
    }
}
