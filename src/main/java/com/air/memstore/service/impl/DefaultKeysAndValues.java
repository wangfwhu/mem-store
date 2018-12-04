package com.air.memstore.service.impl;

import com.air.memstore.exception.ErrorListener;
import com.air.memstore.service.AtomicDataProcessor;
import com.air.memstore.service.KeysAndValues;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: David
 * @Date: 2018-12-03 16:14
 * @Description: Provider the main methods to kvStore and display key-values,
 *
 */
@Data
public class DefaultKeysAndValues implements KeysAndValues {

    //main kvStore
    private final ConcurrentHashMap<String,String> kvStore = new ConcurrentHashMap();

    //kvStore the atomic group data if args of accept() have any atomic data;
    private AtomicDataProcessor atomicDataProcessor;

    ErrorListener errorListener;
    @Override
    public void accept(String kvPairs) {
        if(StringUtils.isEmpty(kvPairs)){
            return;
        }else{
            Map<String,String> acceptedKvPairs = parseKvPairs(kvPairs);
            mergeResults(kvStore,acceptedKvPairs);
        }
    }

    /**
     *
     * Desc: parse the input string,the result is the map of entries:
     *[14=15,A=B52]
     * @param: [kvPairs]
     * @return: java.util.Map<java.lang.String,java.lang.String>
     * @auther: Wangfeng
     * @date: 2018-12-04 18:50
     */
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
                //If it's atomic data, then store it into the processor and continue,will process them together later.
                boolean isAtomicNum = atomicDataProcessor.checkAndSetAtomicData(kvEntry[0].trim(),kvEntry[1].trim());
                if(isAtomicNum){
                    hasAtomicNum = true;
                    continue;
                }
                //Put un-atomic data to result.
                putValueToMap(acceptedKvPairs,kvEntry[0].trim(),kvEntry[1].trim());
            }
        }

        //Process atomic group together specially.
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
     * Desc: merge the new accepted data to existing kvStore.
     *
     * @param: [kvStore, acceptedNewResults]
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
     * Desc: utility to put value to map:
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
        //Put the value directly if not in the kvStore.
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
        keys.addAll(kvStore.keySet());
        for(String key:keys){
            //String displays all key-value pairs (one pair per line)
            resultStrBuilder.append(key).append("=").append(kvStore.get(key)).append("\n");
        }
        String resultStr = resultStrBuilder.toString();
        System.out.println(resultStr);
        return resultStr;
    }
}
