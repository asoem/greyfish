package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.collect.CircularFifoBuffer;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 17.10.11
 * Time: 18:44
 */
public class FixedSizeMessageBox<A extends Agent<A, ?>> extends ForwardingCollection<AgentMessage<A>> implements AgentMessageBox<A>, Serializable {

    private final CircularFifoBuffer<AgentMessage<A>> buffer;

    public FixedSizeMessageBox() {
        this.buffer = CircularFifoBuffer.newInstance(8);
    }

    public FixedSizeMessageBox(int size) {
        this.buffer = CircularFifoBuffer.newInstance(size);
    }

    @Override
    protected Collection<AgentMessage<A>> delegate() {
        return buffer;
    }

    @Override
    public List<AgentMessage<A>> extract(final Predicate<? super AgentMessage<A>> predicate) {
        return ImmutableList.copyOf(Iterables.consumingIterable(Iterables.filter(buffer, predicate)));
    }

    private Object writeReplace() {
        return new SerializedForm<A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    @Override
    public AgentMessage<A> find(Predicate<? super AgentMessage<A>> predicate) throws NoSuchElementException {
        return Iterables.find(buffer, predicate);
    }

    @Override
    public AgentMessage<A> find(Predicate<? super AgentMessage<A>> predicate, AgentMessage<A> defaultValue) {
        return Iterables.find(buffer, predicate, defaultValue);
    }

    @Override
    public Iterable<AgentMessage<A>> filter(Predicate<? super AgentMessage<A>> predicate) {
        return Iterables.filter(buffer, predicate);
    }

    private static class SerializedForm<A extends Agent<A, ?>> implements Serializable {
        private final List<AgentMessage<A>> messages;
        private final int maxSize;

        SerializedForm(FixedSizeMessageBox<A> box) {
            this.messages = Lists.newArrayList(box.buffer);
            this.maxSize = box.buffer.maxSize();
        }

        private Object readResolve() {
            final FixedSizeMessageBox<A> messageBox = new FixedSizeMessageBox<A>(maxSize);
            for (AgentMessage<A> message : messages) {
                messageBox.add(message);
            }
            return messageBox;
        }
    }

    private static final long serialVersionUID = 0;
}
