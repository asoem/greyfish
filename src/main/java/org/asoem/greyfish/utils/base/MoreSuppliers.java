package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;

import java.io.Serializable;

/**
 * User: christoph
 * Date: 26.07.12
 * Time: 10:56
 */
public class MoreSuppliers {

    public static <T> Supplier<T> memoize(Supplier<T> delegate, UpdateRequest<? super T> updateRequest) {
        return new OutdateableMemoizingSupplier<T>(delegate, updateRequest);
    }

    static class OutdateableMemoizingSupplier<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 0;

        private final Supplier<T> delegate;
        private final UpdateRequest<? super T> updateRequest;
        private transient T value;

        OutdateableMemoizingSupplier(Supplier<T> delegate, UpdateRequest<? super T> updateRequest) {
            this.delegate = delegate;
            this.updateRequest = updateRequest;
        }

        @Override
        public T get() {
            synchronized (this) {
                if (updateRequest.apply(value)) {
                    value = delegate.get();
                    updateRequest.done();
                }
                return value;
            }
        }
    }
}
