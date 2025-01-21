package com.c2cb.androidsdk.network;


public interface NetworkEventListener {
   public void OnSuccess(Object object);
   public void OnError(String exception);
}
