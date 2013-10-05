package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements {@code FunctionalCollection} with an underlying {@link FifoBuffer}.
 */
public final class FunctionalFifoBuffer<M> extends ForwardingCollection<M>
        implements Serializable, FunctionalCollection<M> {

    private final FifoBuffer<M> buffer;

    public FunctionalFifoBuffer() {
        this.buffer = FifoBuffer.newInstance(8);
    }

    public FunctionalFifoBuffer(final int size) {
        this.buffer = FifoBuffer.newInstance(size);
    }

    @Override
    protected Collection<M> delegate() {
        return buffer;
    }

    @Override
    public List<M> remove(final Predicate<? super M> predicate) {
        final ImmutableList.Builder<M> ret = ImmutableList.builder();
        for (Iterator<M> iterator = buffer.iterator(); iterator.hasNext(); ) {
            final M message = iterator.next();
            if (predicate.apply(message)) {
                iterator.remove();
                ret.add(message);
            }
        }
        return ret.build();
    }

    @Override
    public Optional<M> findFirst(final Predicate<? super M> predicate) {
        return Optional.fromNullable(Iterables.find(buffer, predicate, null));
    }

    @Override
    public Iterable<M> filter(final Predicate<? super M> predicate) {
        return Iterables.filter(buffer, predicate);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionalFifoBuffer)) return false;

        final FunctionalFifoBuffer that = (FunctionalFifoBuffer) o;

        if (!buffer.equals(that.buffer)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }

    private Object writeReplace() {
        return new SerializedForm<M>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    public static <M> FunctionalFifoBuffer<M> withCapacity(final int size) {
        return new FunctionalFifoBuffer<M>(size);
    }

    @Override
    public boolean any(final Predicate<M> predicate) {
        return Iterables.any(buffer, predicate);
    }

    private static class SerializedForm<M> implements Serializable {
        private final List<M> elements;
        private final int maxSize;

        SerializedForm(final FunctionalFifoBuffer<M> box) {
            this.elements = Lists.newArrayList(box.buffer);
            this.maxSize = box.buffer.capacity();
        }

        private Object readResolve() {
            final FunctionalFifoBuffer<M> messageBox = new FunctionalFifoBuffer<M>(maxSize);
            for (final M message : elements) {
                messageBox.add(message);
            }
            return messageBox;
        }
    }

    private static final long serialVersionUID = 0;
}
