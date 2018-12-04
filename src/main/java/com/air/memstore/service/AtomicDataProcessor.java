package com.air.memstore.service;

import com.air.memstore.exception.ErrorListener;

import java.util.List;
import java.util.Map;

/**
 * @Auther: David
 * @Date: 2018-12-04 15:27
 * @Description:
 */
public interface AtomicDataProcessor {

    boolean checkAndSetAtomicData(String key, String value);
    Map<String,String> checkAndSetAtomicMissing();
    void setErrorListener(ErrorListener errorListener);
    void setCustomizedAtomicGroup(List<String> atomicGroupCust);
}
