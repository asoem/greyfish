package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.utils.collect.CircularFifoBuffer;

import java.util.Iterator;

/**
 * User: christoph
 * Date: 17.10.11
 * Time: 18:44
 */
public class AgentMessageBox implements Iterable<AgentMessage> {

    private final CircularFifoBuffer<AgentMessage> box;

    public AgentMessageBox() {
        this.box = CircularFifoBuffer.newInstance(64);
    }

    public AgentMessageBox(int size) {
        this.box = CircularFifoBuffer.newInstance(size);
    }

    public void push(AgentMessage message) {
        box.add(message);
    }

    public Iterable<AgentMessage> filter(MessageTemplate template) {
        return Iterables.filter(box, template);
    }

    public void clear() {
        box.clear();
    }

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

        AgentMessageBox that = (AgentMessageBox) o;

        if (!box.equals(that.box)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return box.hashCode();
    }
}
