package org.asoem.greyfish.core.acl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import javolution.util.FastList;

import java.util.Iterator;
import java.util.List;

public class PostOffice implements Iterable<ACLMessage> {

    final private int bins = 64;

    final private List<List<ACLMessage>> receiverLists = Lists.newArrayListWithCapacity(bins);

    private PostOffice() {
        for (int i = 0; i < bins; i++) {
            receiverLists.add(FastList.<ACLMessage>newInstance());
        }
    }

    synchronized public void addMessage(ACLMessage message) {
        for (int id : message.getAllReceiver()) {
            int bin = id2bin(id);
            receiverLists.get(bin).add(message);
        }
    }

    synchronized public List<ACLMessage> getMessages(final int receiverId, MessageTemplate messageTemplate) {
        int bin = id2bin(receiverId);
        return Lists.newArrayList(
                Iterables.filter(receiverLists.get(bin),
                        MessageTemplate.and(messageTemplate, MessageTemplate.sentTo(receiverId))));

    }

    synchronized public List<ACLMessage> getMessages(MessageTemplate messageTemplate) {
        return Lists.newArrayList(Iterables.filter(this, messageTemplate));
    }

    private int id2bin(int id) {
       return id & (bins-1);
    }

    public static PostOffice newInstance() {
        return new PostOffice();
    }

    @Override
    public Iterator<ACLMessage> iterator() {
        List<Iterator<ACLMessage>> iteratorList = Lists.newArrayList();
        for (List<ACLMessage> subList : receiverLists)
            iteratorList.add(subList.iterator());
        return Iterators.concat(iteratorList.iterator());
    }
}
