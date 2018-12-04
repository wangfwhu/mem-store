package com.air.calculator.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: David
 * @Date: 2018-12-04 15:14
 * @Description: Support customized atomic group,default is (441,442,500)
 */
@Data
public class AtomicGroupData {

    List<String> atomicGroup;

    public AtomicGroupData() {
        atomicGroup = new ArrayList<>();
        atomicGroup.add(441+"");
        atomicGroup.add(442+"");
        atomicGroup.add(500+"");
    }
}
