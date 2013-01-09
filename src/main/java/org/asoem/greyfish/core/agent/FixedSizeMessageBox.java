package org.asoem.greyfish.core.agent;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.utils.collect.CircularFifoBuffer;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * User: christoph
 * Date: 17.10.11
 * Time: 18:44
 */
public class FixedSizeMessageBox<A extends Agent<A, ?>> implements AgentMessageBox<A>, Serializable {

    private final CircularFifoBuffer<AgentMessage<A>> box;

    public FixedSizeMessageBox() {
        this.box = CircularFifoBuffer.newInstance(8);
    }

    public FixedSizeMessageBox(int size) {
        this.box = CircularFifoBuffer.newInstance(size);
    }

    @Override
    public void push(AgentMessage<A> message) {
        box.add(message);
    }

    @Override
    public Iterable<AgentMessage<A>> filter(MessageTemplate template) {
        return Iterables.filter(box, template);
    }

    @Override
    public void clear() {
        box.clear();
    }

    @Override
    public void pushAll(Iterable<? extends AgentMessage<A>> message) {
        Iterables.addAll(box, message);
    }

    @Override
    public Iterator<AgentMessage<A>> iterator() {
        return box.iterator();
    }

    @Override
    public List<AgentMessage<A>> consume(final MessageTemplate template) {
        return ImmutableList.copyOf(
                new AbstractIterator<AgentMessage<A>>() {

                    final ListIterator<AgentMessage<A>> listIterator = box.listIterator();

                    @Override
                    protected AgentMessage<A> computeNext() {
                        while (listIterator.hasNext()) {
                            final AgentMessage<A> message = listIterator.next();
                            if (template.apply(message)) {
                                listIterator.remove();
                                return message;
                            }
                        }

                        return endOfData();
                    }
                });
    }

    @Override
    public List<AgentMessage<A>> messages() {
        return box;
    }

    private Object writeReplace() {
        return new SerializedForm<A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializedForm<A extends Agent<A, ?>> implements Serializable {
        private List<AgentMessage<A>> messages;

        SerializedForm(FixedSizeMessageBox<A> box) {
            this.messages = box.messages();
        }

        private Object readResolve() {
            final FixedSizeMessageBox<A> messageBox = new FixedSizeMessageBox<A>(messages.size());
            for (AgentMessage<A> message : messages) {
                messageBox.push(message);
            }
            return messageBox;
        }
    }

    private static final long serialVersionUID = 0;
}
