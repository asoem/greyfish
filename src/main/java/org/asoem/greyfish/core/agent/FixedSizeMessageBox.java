package org.asoem.greyfish.core.agent;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.utils.collect.CircularFifoBuffer;

import java.util.Iterator;
import java.util.ListIterator;

/**
 * User: christoph
 * Date: 17.10.11
 * Time: 18:44
 */
public class FixedSizeMessageBox implements AgentMessageBox {

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FixedSizeMessageBox that = (FixedSizeMessageBox) o;

        if (!box.equals(that.box)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return box.hashCode();
    }

    @Override
    public Iterable<AgentMessage> consume(final MessageTemplate template) {
        return new Iterable<AgentMessage>() {
            @Override
            public Iterator<AgentMessage> iterator() {
                return new AbstractIterator<AgentMessage>() {

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
                };
            }
        };
    }
}
