package org.asoem.greyfish.lang;

import com.google.common.collect.ForwardingIterator;

import java.util.Iterator;

public abstract class CompositeIterator<T> extends ForwardingIterator<T> {
    final int depth;

    private CompositeIterator(int depth) {
        this.depth = depth;
    }

    public static <T> CompositeIterator<T> newInstance(final Iterator<T> iter, int depth) {
          return new CompositeIterator<T>(depth) {
              @Override
              protected Iterator<T> delegate() {
                  return iter;
              }
          };
    }
}
