package com.jd.sirector.rocket.utils;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);

    public Utils() {
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(Collection coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNotEmpty(Collection coll) {
        return !isEmpty(coll);
    }

    public static void append(StringBuilder result, Object obj) {
        if (obj != null) {
            result.append(obj);
        } else {
            result.append("null");
        }

    }

    public static String firstUpper(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    public static ExecutorService getPool() {
        return POOL;
    }
}
