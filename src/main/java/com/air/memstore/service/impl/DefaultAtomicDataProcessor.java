package com.air.memstore.service.impl;

import com.air.memstore.exception.ErrorListener;
import com.air.memstore.exception.DefaultErrorListener;
import com.air.memstore.service.AtomicDataProcessor;
import com.air.memstore.service.AtomicGroupData;
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

    //Store the atomic keys and values from the accept() method, and then check the atomic data missing or not later.
    //if accept("441= one,442=1,442=4"):
    //then here is:[441=[one,1],442=[4]]
    Map<String, List<String>> keyAndValuesForAtomic = new HashMap<>();
    //As the requirement sample,if there are multiple complete atomic data, use the last complete one to overwrite previous ones:
    //Atomic group specified twice
    //kv.accept("18=zzz,441=one,500=three,442=2,442= A,441 =3,35=D,500=ok  ")
    //18=zzz
    //35=D
    //441=3
    //442=A
    //500=ok
    private int indexOfLastCompleteAtomicGroup;
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
                //Store all atomic data and process them later.
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
     * any missing atomic data missing will call error listener to generate the error msg.
     *
     * @param: []
     * @return: java.util.Map<java.lang.String,java.lang.String>
     * @auther: Wangfeng
     * @date: 2018-12-04 12:30
     */
    @Override
    public Map<String,String> checkAndSetAtomicMissing(){
        //If no any atomic data, then the process ends.
        if(keyAndValuesForAtomic.isEmpty()){
            return null;
        }
        Set<String> atomicKeys = keyAndValuesForAtomic.keySet();
        Map<String,String> fullAtomicNumMap = null;
        //Check whether there is full atomic group, and any atomic data missing will call error listener to gen the error msg.
        boolean hasFullAtomicNum = true;

        //Times for each atomic data from the accepted string:
        //kv.accept("500= three , 6 = 7 ,441= one,442=1,442=4")
        //[500=1,441=1,442=2]
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

        //Find out the missing atomic data using the key times we got in previous step.
        List<String> missingAtomicKeys = getMissingAtomicKeys(keyTimes);
        if(missingAtomicKeys !=null && missingAtomicKeys.size() > 0){
            String missingMsg = buildMissingMsg(missingAtomicKeys);
            errorListener.onError(missingMsg);
        }

        //Concat full atomic group data str.
        if(hasFullAtomicNum){
            fullAtomicNumMap = new HashMap<>();
            for (String num:atomicKeys) {
                //use the last full one to overwrite previous entries
                fullAtomicNumMap.put(num+"",keyAndValuesForAtomic.get(num).get(indexOfLastCompleteAtomicGroup -1));
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
    public void setCustomizedAtomicGroup(List<String> atomicGroupCust) {
        getAtomicGroupdata().setAtomicGroup(atomicGroupCust);
    }

    private List<String> getMissingAtomicKeys(Map<String,Integer> keyTimes) {
        List<String> resultMissingKeys = new ArrayList<>();

        //Times for all atomic data.
        //If kv.accept("500= three , 6 = 7 ,441= one,442=1,442=4")
        //Then keyTimes is :[500=1,441=1,442=2]
        //Then numOfKeys is the list:[1,1,2]
        List<Integer> numOfKeys = new ArrayList<>();
        for(String definedAtomicKey: atomicGroupdata.getAtomicGroup()){
            if(keyTimes.containsKey(definedAtomicKey)){
                numOfKeys.add(keyTimes.get(definedAtomicKey));
            }else{
                //Incomplete atomic group.
                resultMissingKeys.add(definedAtomicKey);
            }
        }

        if(resultMissingKeys.size() > 0){
            return resultMissingKeys;
        }

        //The min is num of complete atomic group data actually.
        //If the max == min, means all atomic group data are complete.
        int max = Collections.max(numOfKeys);
        int min = Collections.min(numOfKeys);

        //If kv.accept("18=zzz,441=one,500=three,442=2,442= A,441 =3,35=D,500=ok  ")
        //Then numOfKeys is the list:[2,2,2]
        //The result is
        // 18=zzz
        // 35=D
        // 441=3
        // 442=A
        // 500=ok
        // indexOfLastCompleteAtomicGroup is used as the index to get the last full atomic data.
        // 441=3,442=A,500=ok is the final atomic data to store as the sample in the comments.
        indexOfLastCompleteAtomicGroup = min;
        //max == min,means no incomplete atomic data.
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
