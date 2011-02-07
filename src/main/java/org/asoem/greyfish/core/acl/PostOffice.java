package org.asoem.greyfish.core.acl;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import javolution.util.FastList;
import org.asoem.greyfish.core.individual.Agent;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Iterables.consumingIterable;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

public class PostOffice implements Iterable<ACLMessage> {

    final private int bins = 64;

    final private List<FastList<ACLMessage>> receiverLists = Lists.newArrayListWithCapacity(bins);

    private int messageCounter;

    private PostOffice() {
        for (int i = 0; i < bins; i++) {
            receiverLists.add(FastList.<ACLMessage>newInstance());
        }
    }

    synchronized public void addMessage(ACLMessage message) {
        for (int id : message.getAllReceiver()) {
            ++messageCounter;
            int bin = id2bin(id);
            receiverLists.get(bin).add(message);
        }
    }

    synchronized public List<ACLMessage> pollMessages(final int receiverId, final MessageTemplate messageTemplate) {
        int bin = id2bin(receiverId);
        return pollMessagesInBin(bin, messageTemplate);

    }

    private synchronized List<ACLMessage> pollMessagesInBin(final int bin, final MessageTemplate messageTemplate) {
        if (messageTemplate.equals(MessageTemplate.alwaysFalse()))
            return ImmutableList.of();
        final List<ACLMessage> ret = Lists.newArrayList();
        final FastList<ACLMessage> messages = receiverLists.get(bin);
        for (FastList.Node<ACLMessage> n = messages.head(), end = messages.tail(); (n = n.getNext()) != end;) {
            if (messageTemplate.apply(n.getValue())) {
                ret.add(n.getValue());
                FastList.Node<ACLMessage> p = n.getPrevious();
                messages.delete(n);
                n = p;
            }
        }
        messageCounter -= ret.size();
        return ret;
    }

    synchronized public List<ACLMessage> getMessages(final MessageTemplate messageTemplate) {
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

    public int getMessageCounter() {
        return messageCounter;
    }
}
