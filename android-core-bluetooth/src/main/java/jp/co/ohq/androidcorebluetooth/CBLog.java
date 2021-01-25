//
//  CBLog.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.ohq.androidcorebluetooth;

import jp.co.ohq.utility.log.AbsLog;

class CBLog extends AbsLog {

    public static final String TAG = CBLog.class.getSimpleName();
    public static final boolean OUTPUT_LOG_ENABLED = true;
    private static final int STACK_DEPTH = 5;
    private static final LogLevel OUTPUT_LOG_LEVEL = LogLevel.Verbose;
    private static final boolean OUTPUT_THREAD_NAME = true;
    private static final String LOG_PREFIX_OS_API = "[OS_API]";

    public static void d(String msg) {
        outputLog(LogLevel.Debug, LOG_PREFIX_DEBUG, msg);
    }

    public static void i(String msg) {
        outputLog(LogLevel.Info, LOG_PREFIX_INFO, msg);
    }

    public static void w(String msg) {
        outputLog(LogLevel.Warn, LOG_PREFIX_WARN, msg);
    }

    public static void e(String msg) {
        outputLog(LogLevel.Error, LOG_PREFIX_ERROR, msg);
    }

    public static void vMethodIn() {
        outputLog(LogLevel.Verbose, LOG_PREFIX_METHOD_IN, "");
    }

    public static void vMethodIn(String msg) {
        outputLog(LogLevel.Verbose, LOG_PREFIX_METHOD_IN, msg);
    }

    public static void vMethodOut() {
        outputLog(LogLevel.Verbose, LOG_PREFIX_METHOD_OUT, "");
    }

    public static void vMethodOut(String msg) {
        outputLog(LogLevel.Verbose, LOG_PREFIX_METHOD_OUT, msg);
    }

    public static void iOsApi(String msg) {
        outputLog(LogLevel.Info, LOG_PREFIX_OS_API, msg);
    }

    private static void outputLog(LogLevel level, String prefix, String msg) {
        outputLog(level, prefix + " " + methodNameString(STACK_DEPTH) + " " + msg);
    }

    private static void outputLog(LogLevel level, String msg) {
        if (!OUTPUT_LOG_ENABLED) {
            return;
        }
        if (OUTPUT_LOG_LEVEL.ordinal() > level.ordinal()) {
            return;
        }
        outputLog(TAG, level, OUTPUT_THREAD_NAME, msg);
    }
}
