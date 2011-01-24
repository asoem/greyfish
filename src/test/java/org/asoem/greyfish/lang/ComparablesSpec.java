package org.asoem.greyfish.lang;

import com.google.common.collect.ImmutableList;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(JDaveRunner.class)
public class ComparablesSpec extends Specification<Comparables> {
    public class OrderedComparables {
        private List<Integer> comparables = ImmutableList.of(1,2,3,4,5,6,7);
        public void shouldBeInOrderForSizeGreaterThan1() {
            specify(Comparables.areInOrder(comparables), must.equal(true));
        }

        public void shouldBeInOrderForSize0() {
            specify(Comparables.areInOrder(ImmutableList.<Integer>of()), must.equal(true));
        }

        public void shouldBeInOrderForSize1() {
            specify(Comparables.areInOrder(comparables.get(0)), must.equal(true));
        }
    }
}
