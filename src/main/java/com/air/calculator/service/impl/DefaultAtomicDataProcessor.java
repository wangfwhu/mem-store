package com.air.calculator.service.impl;

import com.air.calculator.exception.ErrorListener;
import com.air.calculator.exception.DefaultErrorListener;
import com.air.calculator.service.AtomicDataProcessor;
import com.air.calculator.service.AtomicGroupData;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @Auther: David
 * @Date: 2018-12-03 19:53
 * @Description: process the atomic group data.
 */
@Data
public class DefaultAtomicDataProcessor implements AtomicDataProcessor {

    //Store the atomic keys and its value from the accept() method, and then check the missing or not later.
    //accept("441= one,442=1,442=4"):
    //[441=[one,1],442=[4]]
    Map<String, List<String>> keyAndValuesForAtomic = new HashMap<>();
    //If atomic group specified multiple times, need overwrite previous values.
    private int numOfAtomicCompleteGroup;
    ErrorListener errorListener;
    AtomicGroupData atomicGroupdata;

    public DefaultAtomicDataProcessor(){
        atomicGroupdata = new AtomicGroupData();
        errorListener = new DefaultErrorListener();
    }

    @Override
    public boolean checkAndSetAtomicData(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return false;
        } else {
            boolean isAtomicData = atomicGroupdata.getAtomicGroup().contains(key);
            if(isAtomicData){
                List result;
                if(keyAndValuesForAtomic.containsKey(key)){
                    result = keyAndValuesForAtomic.get(key);
                    result.add(value);
                    keyAndValuesForAtomic.put(key,result);
                }else{
                    result = new ArrayList();
                    result.add(value);
                    keyAndValuesForAtomic.put(key,result);
                }
            }
            return isAtomicData;
        }
    }

    /**
     *
     * Desc: return the values for atomic group if there is at least one full atomic group,
     * any missing atomic data missing will display the error msg.
     *
     * @param: []
     * @return: java.util.Map<java.lang.String,java.lang.String>
     * @auther: Wangfeng
     * @date: 2018-12-04 12:30
     */
    @Override
    public Map<String,String> checkAndSetAtomicMissing(){
        if(keyAndValuesForAtomic.isEmpty()){
            return null;
        }
        Set<String> atomicKeys = keyAndValuesForAtomic.keySet();
        Map<String,String> fullAtomicNumMap = null;
        //check whether there is full atomic group, otherwise any atomic data missing will show the wrong msg.
        boolean hasFullAtomicNum = true;
        Map<String,Integer> keyTimes = new HashMap<>();
        for (String key:atomicGroupdata.getAtomicGroup()) {
            List valueList = keyAndValuesForAtomic.get(key);
            if(valueList != null){
                keyTimes.put(key,keyAndValuesForAtomic.get(key).size());
            }else{
                //Incomplete group if any atomic group data missing.
                hasFullAtomicNum = false;
            }
        }

        List<String> missingAtomicKeys = getMissingAtomicKeys(keyTimes);
        String missingMsg = buildMissingMsg(missingAtomicKeys);
        errorListener.onError(missingMsg);
        //concat full atomic num str.
        if(hasFullAtomicNum){
            fullAtomicNumMap = new HashMap<>();
            for (String num:atomicKeys) {
                fullAtomicNumMap.put(num+"",keyAndValuesForAtomic.get(num).get(numOfAtomicCompleteGroup-1));
            }
        }
        return fullAtomicNumMap;
    }

    /**
     *
     * Desc: Support customized atomic group, data can be alphanumeric.
     *
     * @param: [atomicGroupCust]
     * @return: void
     * @auther: Wangfeng
     * @date: 2018-12-04 15:35
     */
    @Override
    public void setCustmizedAtomicGroup(List<String> atomicGroupCust) {
        getAtomicGroupdata().setAtomicGroup(atomicGroupCust);
    }

    private List<String> getMissingAtomicKeys(Map<String,Integer> keyTimes) {
        List<String> resultMissingKeys = new ArrayList<>();
        List<Integer> numOfKeys = new ArrayList<>();
        for(String definedAtomicKey: atomicGroupdata.getAtomicGroup()){
            if(keyTimes.containsKey(definedAtomicKey)){
                numOfKeys.add(keyTimes.get(definedAtomicKey));
            }else{
                //incomplete atomic group.
                resultMissingKeys.add(definedAtomicKey);
            }
        }

        if(resultMissingKeys.size() > 0){
            return resultMissingKeys;
        }

        int max = Collections.max(numOfKeys);
        int min = Collections.min(numOfKeys);
        numOfAtomicCompleteGroup = min;
        //All atomic data should occur as complete group,so if max == min means all are complete group.
        if(max == min){
            return null;
        }else{
            for(Map.Entry<String,Integer> entry:keyTimes.entrySet()){
                if(entry.getValue() == min){
                    resultMissingKeys.add(entry.getKey());
                }
            }
        }
        return resultMissingKeys;
    }

    private String buildMissingMsg(List<String> missingAtomicNums) {
        //The missing atomic msg is like:atomic group(441,442,500) missing 442
        StringBuilder msg = new StringBuilder("atomic group(");
        msg.append(StringUtils.join(atomicGroupdata.getAtomicGroup(),","));
        msg.append(") missing ");
        msg.append(StringUtils.join(missingAtomicNums,","));
        return msg.toString();
    }
}
