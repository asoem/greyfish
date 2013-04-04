package org.asoem.greyfish.core.acl;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.collect.FifoBuffer;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 17.10.11
 * Time: 18:44
 */
public class FixedSizeMessageBox<M extends ACLMessage<?>> extends ForwardingCollection<M> implements MessageBox<M>, Serializable {

    private final FifoBuffer<M> buffer;

    public FixedSizeMessageBox() {
        this.buffer = FifoBuffer.newInstance(8);
    }

    public FixedSizeMessageBox(int size) {
        this.buffer = FifoBuffer.newInstance(size);
    }

    @Override
    protected Collection<M> delegate() {
        return buffer;
    }

    @Override
    public List<M> extract(final Predicate<? super M> predicate) {
        final ImmutableList.Builder<M> ret = ImmutableList.builder();
        for (Iterator<M> iterator = buffer.iterator(); iterator.hasNext(); ) {
            M message = iterator.next();
            if (predicate.apply(message)) {
                iterator.remove();
                ret.add(message);
            }
        }
        return ret.build();
    }

    @Override
    public M find(Predicate<? super M> predicate) throws NoSuchElementException {
        return Iterables.find(buffer, predicate);
    }

    @Override
    public M find(Predicate<? super M> predicate, M defaultValue) {
        return Iterables.find(buffer, predicate, defaultValue);
    }

    @Override
    public Iterable<M> filter(Predicate<? super M> predicate) {
        return Iterables.filter(buffer, predicate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FixedSizeMessageBox)) return false;

        FixedSizeMessageBox that = (FixedSizeMessageBox) o;

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

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    public static <M extends ACLMessage<?>> FixedSizeMessageBox<M> withCapacity(int size) {
        return new FixedSizeMessageBox<M>(size);
    }

    @Override
    public boolean any(Predicate<M> predicate) {
        return Iterables.any(buffer, predicate);
    }

    private static class SerializedForm<M extends ACLMessage<?>> implements Serializable {
        private final List<M> messages;
        private final int maxSize;

        SerializedForm(FixedSizeMessageBox<M> box) {
            this.messages = Lists.newArrayList(box.buffer);
            this.maxSize = box.buffer.capacity();
        }

        private Object readResolve() {
            final FixedSizeMessageBox<M> messageBox = new FixedSizeMessageBox<M>(maxSize);
            for (M message : messages) {
                messageBox.add(message);
            }
            return messageBox;
        }
    }

    private static final long serialVersionUID = 0;
}
