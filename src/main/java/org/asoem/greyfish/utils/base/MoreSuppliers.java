package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;

import java.io.Serializable;

/**
 * User: christoph
 * Date: 26.07.12
 * Time: 10:56
 */
@Deprecated
public final class MoreSuppliers {

    private MoreSuppliers() {}

    @Deprecated
    public static <T> Supplier<T> memoize(Supplier<T> delegate, UpdateRequest<? super T> updateRequest) {
        return new OutdateableMemoizingSupplier<T>(delegate, updateRequest);
    }

    @Deprecated
    static class OutdateableMemoizingSupplier<T> implements Supplier<T>, Serializable {
        private static final long serialVersionUID = 0;

        private final Supplier<T> delegate;
        private final UpdateRequest<? super T> updateRequest;
        private transient T value;

        private OutdateableMemoizingSupplier(Supplier<T> delegate, UpdateRequest<? super T> updateRequest) {
            this.delegate = delegate;
            this.updateRequest = updateRequest;
        }

        @Override
        public T get() {
            synchronized (updateRequest) {
                if (updateRequest.isOutdated(value)) {
                    value = delegate.get();
                    updateRequest.updated();
                }
            }
            return value;
        }
    }
}
