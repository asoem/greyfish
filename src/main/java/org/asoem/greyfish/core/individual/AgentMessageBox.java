package org.asoem.greyfish.core.individual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.lang.CircularFifoBuffer;

import java.util.Iterator;
import java.util.List;

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

    public List<AgentMessage> pull(MessageTemplate template) {
        return ImmutableList.copyOf(Iterables.filter(box, template));
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
}
