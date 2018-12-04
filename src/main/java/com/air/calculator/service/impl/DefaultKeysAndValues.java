package com.air.calculator.service.impl;

import com.air.calculator.exception.ErrorListener;
import com.air.calculator.service.AtomicDataProcessor;
import com.air.calculator.service.KeysAndValues;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: David
 * @Date: 2018-12-03 16:14
 * @Description: Provider the main methods to store and display key-values,
 *
 */
@Data
public class DefaultKeysAndValues implements KeysAndValues {

    //main store
    private final ConcurrentHashMap<String,String> store = new ConcurrentHashMap();

    //store the atomic group data if args of accept() have any atomic data;
    private AtomicDataProcessor atomicDataProcessor;

    ErrorListener errorListener;
    @Override
    public void accept(String kvPairs) {
        if(StringUtils.isEmpty(kvPairs)){
            return;
        }else{
            Map<String,String> acceptedKvPairs = parseKvPairs(kvPairs);
            mergeResults(store,acceptedKvPairs);
        }
    }

    private Map<String,String> parseKvPairs(String kvPairs){
        Map<String,String> acceptedKvPairs = new HashMap<>();
        boolean hasAtomicNum = false;
        String[] kvArray = StringUtils.split(kvPairs.trim(),",");
        for(String kv:kvArray){
            String[] kvEntry = StringUtils.split(kv.trim(),"=");
            if(kvEntry != null){
                if(!StringUtils.isAlphanumeric(kvEntry[0].trim())){
                    errorListener.onError("only alphanumeric keys are allowed,invalid key:"+kvEntry[0].trim());
                    continue;
                }
                //If it's atomic num, then put into the repo and skip it, will process them later.
                boolean isAtomicNum = atomicDataProcessor.checkAndSetAtomicData(kvEntry[0].trim(),kvEntry[1].trim());
                if(isAtomicNum){
                    hasAtomicNum = true;
                    continue;
                }
                //Put un-atomic data to result.
                putValueToMap(acceptedKvPairs,kvEntry[0].trim(),kvEntry[1].trim());
            }
        }

        //Process atomic group specially.
        if(hasAtomicNum){
            Map<String, String> atomicData = atomicDataProcessor.checkAndSetAtomicMissing();
            if(atomicData != null && atomicData.size() > 0){
                //Put atomic data to result.
                acceptedKvPairs.putAll(atomicData);
            }
        }
        return acceptedKvPairs;
    }

    /**
     *
     * Desc: merge the new accepted data to store.
     *
     * @param: [store, acceptedNewResults]
     * @return: void
     * @auther: Wangfeng
     * @date: 2018-12-04 11:57
     */
    private void mergeResults(Map<String,String> store, Map<String,String> acceptedNewResults) {
        for(String key : acceptedNewResults.keySet()){
            putValueToMap(store,key,acceptedNewResults.get(key).trim());
        }
    }

    /**
     *
     * Desc: put value to map:
     * 1 numeric integer values accumulate.
     * 2 non-integers overwrite.
     *
     * @param: [targetMap, key, value]
     * @return: void
     * @auther: Wangfeng
     * @date: 2018-12-04 12:21
     */
    private void putValueToMap(Map<String,String> targetMap,String key,String value){
        if(targetMap.containsKey(key.trim())){
            if(StringUtils.isNumeric(targetMap.get(key).trim())){
                //Numeric integer values accumulate
                targetMap.put(key.trim(),Integer.parseInt(value) + Integer.parseInt(targetMap.get(key)) + "");
                return;
            }
        }
        //Put the value directly if not in the store.
        //Non-integers overwrite.
        targetMap.put(key.trim(),value);
    }

    @Override
    public String display() {
        StringBuilder resultStrBuilder = new StringBuilder();

        //Keys are sorted (alpha-ascending, case-insensitive)
        Set<String> keys = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String p1, String p2) {
                return p1.toLowerCase().compareTo(p2.toLowerCase());
            }
        });
        keys.addAll(store.keySet());
        for(String key:keys){
            //String displays all key-value pairs (one pair per line)
            resultStrBuilder.append(key).append("=").append(store.get(key)).append("\n");
        }
        String resultStr = resultStrBuilder.toString();
        System.out.println(resultStr);
        return resultStr;
    }

//    public static void main(String[] args){
//        KeysAndValuesDefault kv = new KeysAndValuesDefault();
//        kv.setErrorListener(new ErrorListenerDefault());
//        kv.accept("14=15, 14=7,A=B52, 14 = 4, dry = Don't Repeat Yourself");
//        kv.display();
//        kv.getStore().clear();
//
//        kv.accept("one=two");
//        kv.accept("Three=four");
//        kv.accept("5=6");
//        kv.accept("14=X");
//        kv.display();
//    }
}
