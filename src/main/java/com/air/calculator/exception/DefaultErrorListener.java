package com.air.calculator.exception;

/**
 * @Auther: David
 * @Date: 2018-12-03 19:13
 * @Description:
 */
public class DefaultErrorListener implements ErrorListener{
    @Override
    public void onError(String msg) {
        System.out.println(msg);
    }

    @Override
    public void onError(String msg, Exception e) {
        System.out.println(e.getStackTrace());
        System.out.println(msg);
    }
}
