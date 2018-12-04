package com.air.calculator.service;

import com.air.calculator.exception.ErrorListener;

/**
 * @Auther: David
 * @Date: 2018-12-03 16:13
 * @Description:
 */
public interface KeysAndValues {
    void accept(String kvPairs);
    String display();
    void setErrorListener(ErrorListener errorListener);
}
