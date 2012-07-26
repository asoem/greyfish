package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 25.07.12
 * Time: 18:33
 */
public class LazyObjectImpl<T> implements LazyObject<T> {

    private final Supplier<T> valueSupplier;

    private final UpdateRequest<? super T> updateRequest;

    @Nullable
    private T value = null;

    public LazyObjectImpl(Supplier<T> valueSupplier, UpdateRequest<? super T> updateRequest) {
        this.valueSupplier = checkNotNull(valueSupplier);
        this.updateRequest = checkNotNull(updateRequest);
    }

    @Override
    public T get() {
        updateValue();
        return value;
    }

    @Override
    public void updateValue() {
        if (isOutdated()) {
            value = valueSupplier.get();
            updateRequest.done();
        }
    }

    @Override
    public boolean isOutdated() {
        return updateRequest.apply(value);
    }
}
