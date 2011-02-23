package org.asoem.greyfish.lang;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.acl.PostOffice;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * User: christoph
 * Date: 23.02.11
 * Time: 09:17
 */
@RunWith(JDaveRunner.class)
public class IterablesExtendedSpec  extends Specification<PostOffice> {

    public class ANonEmptyListFilteredByPositions {
        Iterable<Integer> objectList = ImmutableList.of(1,2,3,4,5,6,7,8,9);

        public void mustReturnTheElementsAtThePositionsInOrdeer() {
            List<Integer> positionList = ImmutableList.of(3,4,8);
            Iterable<Integer> filtered = IterablesExtended.filter(objectList, positionList);
            specify(filtered, isNotNull());
            specify(ImmutableList.copyOf(filtered), must.containInOrder(4,5,9));
        }

        public void willThrowAnIndexOutOfBoundExceptionIfAPositionIsInvalid() {
            final List<Integer> positionList = ImmutableList.of(0,14,8);
            specify(new Block() {
                @Override
                public void run() throws Throwable {
                    ImmutableList.copyOf(IterablesExtended.filter(objectList, positionList));
                }
            }, must.raiseExactly(IndexOutOfBoundsException.class));
        }
    }

    public class ANonEmptyNonListFilteredByPositions {
        Iterable<Integer> objectList = ImmutableList.of(1,2,3,4,5,6,7,8,9);

        public void mustReturnTheElementsAtThePositionsInOrdeer() {
            List<Integer> positionList = ImmutableList.of(3,4,8);
            Iterable<Integer> filtered = IterablesExtended.filter(Iterables.unmodifiableIterable(objectList), positionList);
            specify(filtered, isNotNull());
            specify(ImmutableList.copyOf(filtered), must.containInOrder(4,5,9));
        }

        public void willThrowAnIndexOutOfBoundExceptionIfAPositionIsInvalid() {
            final List<Integer> positionList = ImmutableList.of(0,14,8);
            specify(new Block() {
                @Override
                public void run() throws Throwable {
                    ImmutableList.copyOf(IterablesExtended.filter(Iterables.unmodifiableIterable(objectList), positionList));
                }
            }, must.raiseExactly(IndexOutOfBoundsException.class));
        }
    }
}
