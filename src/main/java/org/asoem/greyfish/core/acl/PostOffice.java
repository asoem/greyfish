package org.asoem.greyfish.core.acl;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javolution.util.FastList;
import org.asoem.greyfish.utils.FastLists;

import java.util.List;

public class PostOffice {

    final private int bins = 64;

    final private List<List<ACLMessage>> receiverLists = Lists.newArrayListWithCapacity(bins);

    private PostOffice() {
        for (int i = 0; i < receiverLists.size(); i++) {
            receiverLists.set(i, FastList.<ACLMessage>newInstance());
        }
    }

    synchronized public void addMessage(ACLMessage message) {
        int bin = id2bin(message.getSender());
        receiverLists.get(bin).add(message);
    }

    synchronized public List<ACLMessage> getMessages(final int id) {
        int bin = id2bin(id);
        return Lists.newArrayList(Iterables.filter(receiverLists.get(bin), new Predicate<ACLMessage>() {
            @Override
            public boolean apply(ACLMessage message) {
                return message.getAllReceiver().contains(id);
            }
        }));
    }

    private int id2bin(int id) {
       return id & (bins-1);
    }

    public static PostOffice newInstance() {
        return new PostOffice();
    }
}
