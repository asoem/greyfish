package org.asoem.greyfish.core.acl;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import javolution.util.FastList;
import org.asoem.greyfish.core.individual.MessageReceiver;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterators.transform;
import static com.google.common.collect.Iterators.unmodifiableIterator;

public class PostOffice implements Iterable<ACLMessage> {

    private static final int BINS = 64;

    private final List<List<MessageWrapper>> receiverLists = Lists.newArrayListWithCapacity(BINS);

    private int messageCounter;

    private PostOffice() {
        for (int i = 0; i < BINS; i++) {
            receiverLists.add(FastList.<MessageWrapper>newInstance());
        }
    }

    public synchronized void clear() {
        for (List<MessageWrapper> messageWrappers : receiverLists) {
            messageWrappers.clear();
        }
    }

    public void deliverOrDiscard(Iterable<? extends MessageReceiver> agents) {
        checkNotNull(agents);

        final Map<Integer, MessageReceiver> agentMap = Maps.newHashMap();
        for (int i = 0; i < BINS; i++) {
            for (final MessageWrapper message : receiverLists.get(i)) {
                if (!agentMap.containsKey(message.receiverId)) {
                    MessageReceiver agent = Iterables.find(agents, new Predicate<MessageReceiver>() {
                        @Override
                        public boolean apply(MessageReceiver agent) {
                            return agent.getId() == message.receiverId;
                        }
                    }, null);
                    agentMap.put(message.receiverId, agent);
                }

                MessageReceiver agent = agentMap.get(message.receiverId);
                if (agent != null)
                    agent.pushMessage(message.message);
            }
            agentMap.clear();
            receiverLists.get(i).clear();
        }
    }

    /**
     *
     * @param message the message
     * @return the number of deliveries
     */
    public synchronized int addMessage(ACLMessage message) {
        checkNotNull(message);

        int added = 0;
        for (int id : message.getRecipients()) {
            int bin = id2bin(id);

            if (receiverLists.get(bin).add(new MessageWrapper(message, id))) {
                ++added;
            }
        }

        messageCounter += added;
        return added;
    }

    public synchronized List<ACLMessage> pollMessages(final int receiverId) {
        int bin = id2bin(receiverId);
        return pollMessagesInBin(bin, receiverId);

    }

    private List<ACLMessage> pollMessagesInBin(int bin, int receiverId) {
        if (receiverLists.get(bin).isEmpty())
            return ImmutableList.of();

        final List<ACLMessage> ret = Lists.newArrayList();
        final List<MessageWrapper> messages = receiverLists.get(bin);
        Iterator<MessageWrapper> iterator = messages.listIterator();
        while (iterator.hasNext()) {
            MessageWrapper messageWrapper = iterator.next();
            if (messageWrapper.receiverId == receiverId) {
                ret.add(messageWrapper.message);
                iterator.remove();
            }
        }
        messageCounter -= ret.size();
        return ret;
    }

    private List<ACLMessage> pollMessagesInBin(final int bin, final MessageTemplate messageTemplate) {
        assert messageTemplate != null;

        if (messageTemplate.equals(MessageTemplate.alwaysFalse()))
            return ImmutableList.of();

        final List<ACLMessage> ret = Lists.newArrayList();
        final List<MessageWrapper> messages = receiverLists.get(bin);
        Iterator<MessageWrapper> iterator = messages.listIterator();
        while (iterator.hasNext()) {
            ACLMessage message = iterator.next().message;
            if (messageTemplate.apply(message)) {
                ret.add(message);
                iterator.remove();
            }
        }
        messageCounter -= ret.size();
        return ret;
    }

    public synchronized List<ACLMessage> pollMessages(final MessageTemplate messageTemplate) {
        checkNotNull(messageTemplate);

        final List<ACLMessage> ret = Lists.newArrayList();
        for (int i = 0; i < BINS; i++) {
            ret.addAll(pollMessagesInBin(i, messageTemplate));
        }
        return ret;
    }

    private static int id2bin(int id) {
        return id & (63);
    }

    public static PostOffice newInstance() {
        return new PostOffice();
    }

    public int size() {
        return messageCounter;
    }

    public synchronized void removeAll(int id) {
        final Iterator<MessageWrapper> iterator = receiverLists.get(id2bin(id)).listIterator();
        while (iterator.hasNext()) {
            MessageWrapper messageWrapper = iterator.next();
            if (messageWrapper.receiverId == id) {
                iterator.remove();
            }
        }
    }

    @Override
    public Iterator<ACLMessage> iterator() {
        return unmodifiableIterator(transform(concat(receiverLists).iterator(), new Function<MessageWrapper, ACLMessage>() {
            @Override
            public ACLMessage apply(@Nullable MessageWrapper messageWrapper) {
                assert messageWrapper != null;
                return messageWrapper.message;
            }
        }));
    }

    private final class MessageWrapper {
        private final int receiverId;
        private final ACLMessage message;

        private MessageWrapper(ACLMessage message, int receiverId) {
            this.message = message;
            this.receiverId = receiverId;
        }
    }
}
