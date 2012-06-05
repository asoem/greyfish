package org.asoem.greyfish.utils.collect;

import com.google.common.collect.AbstractIterator;
import org.asoem.greyfish.utils.base.Tuple3;
import org.asoem.greyfish.utils.base.Tuple3Impl;

import java.util.Iterator;

/**
 * User: christoph
 * Date: 05.06.12
 * Time: 13:26
 */
public class Zipped3Impl<E1, S1 extends Iterable<E1>, E2, S2 extends Iterable<E2>, E3, S3 extends Iterable<E3>>
        extends Tuple3Impl<S1, S2, S3> implements Zipped3<E1, S1, E2, S2, E3, S3> {

    public Zipped3Impl(S1 iterable1, S2 iterable2, S3 iterable3) {
        super(iterable1, iterable2, iterable3);
    }

    @Override
    public Iterator<Tuple3<E1, E2, E3>> iterator() {

        return new AbstractIterator<Tuple3<E1, E2, E3>>() {
            private final Iterator<? extends E1> iterator1 = _1().iterator();
            private final Iterator<? extends E2> iterator2 = _2().iterator();
            private final Iterator<? extends E3> iterator3 = _3().iterator();

            @Override
            protected Tuple3<E1, E2, E3> computeNext() {
                if (iterator1.hasNext() && iterator2.hasNext() && iterator3.hasNext())
                    return new Tuple3<E1, E2, E3>() {
                        public E1 e1 = iterator1.next();
                        public E2 e2 = iterator2.next();
                        public E3 e3 = iterator3.next();

                        @Override
                        public E1 _1() {
                            return e1;
                        }

                        @Override
                        public E2 _2() {
                            return e2;
                        }

                        @Override
                        public E3 _3() {
                            return e3;
                        }
                    };
                else
                    return endOfData();

            }
        };
    }
}
