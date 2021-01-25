//
//  CBLog.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.utility.log;

public class UtilLog extends AbsLog {

    private static final boolean OUTPUT_LOG_ENABLED = true;
    private static final int STACK_DEPTH = 6;
    private static final LogLevel OUTPUT_LOG_LEVEL = LogLevel.Verbose;
    private static final boolean OUTPUT_THREAD_NAME = true;
    private static String TAG_DEFAULT = UtilLog.class.getSimpleName();

    public static void d(String tag, String msg) {
        outputLog(tag, LogLevel.Debug, LOG_PREFIX_DEBUG, msg);
    }

    public static void i(String tag, String msg) {
        outputLog(tag, LogLevel.Info, LOG_PREFIX_INFO, msg);
    }

    public static void w(String tag, String msg) {
        outputLog(tag, LogLevel.Warn, LOG_PREFIX_WARN, msg);
    }

    public static void e(String tag, String msg) {
        outputLog(tag, LogLevel.Error, LOG_PREFIX_ERROR, msg);
    }

    public static void vMethodIn(String tag) {
        outputLog(tag, LogLevel.Verbose, LOG_PREFIX_METHOD_IN, "");
    }

    public static void vMethodIn(String tag, String msg) {
        outputLog(tag, LogLevel.Verbose, LOG_PREFIX_METHOD_IN, msg);
    }

    public static void vMethodOut(String tag) {
        outputLog(tag, LogLevel.Verbose, LOG_PREFIX_METHOD_OUT, "");
    }

    public static void vMethodOut(String tag, String msg) {
        outputLog(tag, LogLevel.Verbose, LOG_PREFIX_METHOD_OUT, msg);
    }

    private static void outputLog(String tag, LogLevel level, String prefix, String msg) {
        outputLog(tag, level, prefix + " " + methodNameString(STACK_DEPTH) + " " + msg);
    }

    private static void outputLog(String tag, LogLevel level, String msg) {
        if (!OUTPUT_LOG_ENABLED) {
            return;
        }
        if (OUTPUT_LOG_LEVEL.ordinal() > level.ordinal()) {
            return;
        }
        outputLog(null == tag ? TAG_DEFAULT : tag, level, OUTPUT_THREAD_NAME, msg);
    }
}
