package org.asoem.greyfish.lang;


import com.google.common.collect.Iterators;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.util.Iterator;

@RunWith(JDaveRunner.class)
public class ArraysArrayIteratorSpec extends Specification<ArraysArrayIteratorSpec> {

    public class AnArraysArrayIteratorOverAnEmptyArray {
        public void shouldReturnNoEntry() {
            Integer[][] array = new Integer[0][0];
            Iterator<Integer> iterable = new ArraysArrayIterator<Integer>(array);
            specify(Iterators.size(iterable), must.equal(0));
        }

    }

    public class AnArraysArrayIteratorOverAnArrayWithNEntriesInOrder {
        public void shouldReturnNEntriesInOrder() {
            Integer[][] array = {
                    {1,2,3},
                    {},
                    {4},
                    {5,6}
            };
            Iterator<Integer> iterable = new ArraysArrayIterator<Integer>(array);
            specify(Iterators.toArray(iterable, Integer.class), must.containInOrder(1,2,3,4,5,6));
        }

    }
}
