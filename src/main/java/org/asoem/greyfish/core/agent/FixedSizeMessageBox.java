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
public class FixedSizeMessageBox implements AgentMessageBox, Serializable {

    private final CircularFifoBuffer<AgentMessage> box;

    public FixedSizeMessageBox() {
        this.box = CircularFifoBuffer.newInstance(8);
    }

    public FixedSizeMessageBox(int size) {
        this.box = CircularFifoBuffer.newInstance(size);
    }

    @Override
    public void push(AgentMessage message) {
        box.add(message);
    }

    @Override
    public Iterable<AgentMessage> filter(MessageTemplate template) {
        return Iterables.filter(box, template);
    }

    @Override
    public void clear() {
        box.clear();
    }

    @Override
    public void pushAll(Iterable<? extends AgentMessage> message) {
        Iterables.addAll(box, message);
    }

    @Override
    public Iterator<AgentMessage> iterator() {
        return box.iterator();
    }

    @Override
    public List<AgentMessage> consume(final MessageTemplate template) {
        return ImmutableList.copyOf(
                new AbstractIterator<AgentMessage>() {

                    final ListIterator<AgentMessage> listIterator = box.listIterator();

                    @Override
                    protected AgentMessage computeNext() {
                        while (listIterator.hasNext()) {
                            final AgentMessage message = listIterator.next();
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
    public List<AgentMessage> messages() {
        return box;
    }

    private Object writeReplace() {
        return new SerializedForm(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializedForm implements Serializable {
        private AgentMessage[] messages;

        SerializedForm(FixedSizeMessageBox box) {
            this.messages = box.messages().toArray(new AgentMessage[box.messages().size()]);
        }

        private Object readResolve() {
            final FixedSizeMessageBox messageBox = new FixedSizeMessageBox(messages.length);
            for (AgentMessage message : messages) {
                messageBox.push(message);
            }
            return messageBox;
        }
    }

    private static final long serialVersionUID = 0;
}
