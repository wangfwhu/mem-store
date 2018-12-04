package com.air.memstore;

import com.air.memstore.exception.DefaultErrorListener;
import com.air.memstore.service.AtomicDataProcessor;
import com.air.memstore.service.impl.DefaultAtomicDataProcessor;
import com.air.memstore.service.impl.DefaultKeysAndValues;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: David
 * @Date: 2018-12-02 19:20
 * @Description: Test classes, besides the test cases ref to the examples of the readme.md,
 * adding customized atomic group cases as last 3 cases.
 */
public class KeysAndValuesTestCase {

	DefaultKeysAndValues kv;
	@Before
	public void init(){
		AtomicDataProcessor atomicDataProcessor = new DefaultAtomicDataProcessor();
		atomicDataProcessor.setErrorListener(new DefaultErrorListener());
		kv = new DefaultKeysAndValues();
		kv.setAtomicDataProcessor(new DefaultAtomicDataProcessor());
		kv.setErrorListener(new DefaultErrorListener());
	}

	@Test
	public void testExample1() {
		System.out.println("Start testExample1");

		kv.accept("14=15, 14=7,A=B52, 14 = 4, dry = Don't Repeat Yourself");
		kv.display();
	}

	@Test
	public void testExample2() {
		System.out.println("Start testExample2");

		kv.accept("one=two");
		kv.accept("Three=four");
		kv.accept("5=6");
		kv.accept("14=X");
		kv.display();
	}

	@Test
	public void testSingleAtomicGroup() {
		System.out.println("Start testSingleAtomicGroup");

		kv.accept("441=one,X=Y, 442=2,500=three");
		kv.display();
	}

	@Test
	public void testAtomicGroupSpecifiedTwice() {
		System.out.println("Start testAtomicGroupSpecifiedTwice");

		kv.accept("18=zzz,441=one,500=three,442=2,442= A,441 =3,35=D,500=ok  ");
		kv.display();
	}

	@Test
	public void testIncompleteGroup() {
		System.out.println("Start testIncompleteGroup");

		kv.accept("441=3,200=not ok,13=qwerty");
		kv.display();
	}

	@Test
	public void testSecondGroupIncomplete() {
		System.out.println("Start testSecondGroupIncomplete");

		kv.accept("500= three , 6 = 7 ,441= one,442=1,442=4");
		kv.display();
	}

	@Test
	public void testInvalidKey() {
		System.out.println("Start testInvalidKey");

		kv.accept("5_0= three , 6* = 7 ,4= one,442=1,442=4");
		kv.display();
	}

	@Test
	public void testCustomizedAtomicGroup1() {
		System.out.println("Start testCustomizedAtomicGroup1");

		List<String> atomicGroupCust = new ArrayList<>();
		atomicGroupCust.add("atomic1");
		atomicGroupCust.add("14");
		atomicGroupCust.add("dry");

		kv.getAtomicDataProcessor().setCustmizedAtomicGroup(atomicGroupCust);

		kv.accept("14=15, 14=7,A=B52, 14 = 4, dry = Don't Repeat Yourself");
		kv.display();
	}

	@Test
	public void testCustomizedAtomicGroup2() {
		System.out.println("Start testCustomizedAtomicGroup2");

		List<String> atomicGroupCust = new ArrayList<>();
		atomicGroupCust.add("atomic1");
		atomicGroupCust.add("14");
		atomicGroupCust.add("dry");

		kv.getAtomicDataProcessor().setCustmizedAtomicGroup(atomicGroupCust);

		kv.accept("14=15, atomic1=7,A=B52, 14 = 4, dry = Don't Repeat Yourself");
		kv.display();
	}

	@Test
	public void testCustomizedAtomicGroup3() {
		System.out.println("Start testCustomizedAtomicGroup3");

		List<String> atomicGroupCust = new ArrayList<>();
		atomicGroupCust.add("441");
		atomicGroupCust.add("442");
		atomicGroupCust.add("500");

		kv.getAtomicDataProcessor().setCustmizedAtomicGroup(atomicGroupCust);

		kv.accept("500= three , 6 = 7 ,441= one,442=1,442=4");
		kv.display();
	}
}
