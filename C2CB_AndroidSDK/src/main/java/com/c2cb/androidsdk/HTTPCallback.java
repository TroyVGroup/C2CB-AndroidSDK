package com.c2cb.androidsdk;

public interface HTTPCallback {
    void processFinish(Object clazz);
    void processFailed(int responseCode, String output);
}
