package org.asoem.greyfish.core.acl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.asoem.greyfish.lang.CircularFifoBuffer;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class PostOffice implements Iterable<ACLMessage> {

    final private int bins = 64;

    final private List<List<ACLMessage>> receiverLists = Lists.newArrayListWithCapacity(bins);

    private int messageCounter;

    private PostOffice() {
        for (int i = 0; i < bins; i++) {
            receiverLists.add(CircularFifoBuffer.<ACLMessage>newInstance(32));
        }
    }

    synchronized public void addMessage(ACLMessage message) {
        for (int id : message.getAllReceiver()) {
            int bin = id2bin(id);
            int sizeBefore = receiverLists.get(bin).size();
            receiverLists.get(bin).add(message);
            int sizeAfter = receiverLists.get(bin).size();
            if (sizeAfter > sizeBefore)
                ++messageCounter;
        }
    }

    synchronized public List<ACLMessage> pollMessages(final int receiverId, final MessageTemplate messageTemplate) {
        int bin = id2bin(receiverId);
        return pollMessagesInBin(bin, messageTemplate);

    }

    private List<ACLMessage> pollMessagesInBin(final int bin, final MessageTemplate messageTemplate) {
        if (messageTemplate.equals(MessageTemplate.alwaysFalse()))
            return ImmutableList.of();

        final List<ACLMessage> ret = Lists.newArrayList();
        final List<ACLMessage> messages = receiverLists.get(bin);
        Iterator<ACLMessage> iterator = messages.listIterator();
        while (iterator.hasNext()) {
            ACLMessage message = iterator.next();
              if (messageTemplate.apply(message)) {
                  ret.add(message);
                  iterator.remove();
              }
        }
        messageCounter -= ret.size();
        return ret;
    }

    synchronized public List<ACLMessage> pollMessages(final MessageTemplate messageTemplate) {
        final List<ACLMessage> ret = Lists.newArrayList();
        for (int i = 0; i < bins; i++) {
             ret.addAll(pollMessagesInBin(i, messageTemplate));
        }
        return ret;
    }

    private int id2bin(int id) {
        return id & (bins-1);
    }

    public static PostOffice newInstance() {
        return new PostOffice();
    }

    @Override
    public Iterator<ACLMessage> iterator() {
        List<Iterator<ACLMessage>> iteratorList = newArrayList();
        for (List<ACLMessage> subList : receiverLists)
            iteratorList.add(subList.iterator());
        return Iterators.concat(iteratorList.iterator());
    }

    public int size() {
        return messageCounter;
    }
}
