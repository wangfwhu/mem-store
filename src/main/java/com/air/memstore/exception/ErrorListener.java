package com.air.memstore.exception;

/**
 * @Auther: David
 * @Date: 2018-12-02 9:01
 * @Description: exception happens when not enough parameters for calc.
 */
public interface ErrorListener{
    void onError(String msg);
    void onError(String msg, Exception e);
}
