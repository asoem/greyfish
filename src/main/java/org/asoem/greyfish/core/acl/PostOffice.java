package org.asoem.greyfish.core.acl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javolution.util.FastList;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.asoem.greyfish.core.individual.MessageReceiver;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterators.unmodifiableIterator;

public class PostOffice implements Iterable<ACLMessage> {

    private static final int BINS = 64; // should be a power of 2

    private final List<List<MessageWrapper>> receiverLists = Lists.newArrayListWithCapacity(BINS);

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    private int messageCounter;

    public PostOffice() {
        for (int i = 0; i < BINS; i++) {
            receiverLists.add(FastList.<MessageWrapper>newInstance());
        }
    }

    public synchronized void clear() {
        for (List<MessageWrapper> messageWrappers : receiverLists) {
            messageWrappers.clear();
        }
        messageCounter = 0;
    }

    /**
     * @param receiverFunction a {@code Function} to get a receiver for a given id. {@code PostOffice} will call this {@code Function} <b>asynchronously</b>,
     * so you should take care of synchronization issues!
     */
    public void deliverOrDiscardAllMessages(final Function<Integer, ? extends MessageReceiver> receiverFunction) {
        checkNotNull(receiverFunction);

        if (size() > 1000) { // TODO: Threshold set just by gut feeling. TEST IT!
            forkJoinPool.invoke(new RecursiveAction() {
                @Override
                protected void compute() {
                    invokeAll(
                            new RecursiveAction() {
                                @Override
                                protected void compute() {
                                    for (MessageWrapper message : concat(receiverLists.subList(0, (BINS / 2) - 1))) {
                                        pushMessageToReceiver(receiverFunction.apply(message.receiverId), message);
                                    }
                                }
                            },
                            new RecursiveAction() {
                                @Override
                                protected void compute() {
                                    for (MessageWrapper message : concat(receiverLists.subList((BINS / 2), BINS - 1))) {
                                        pushMessageToReceiver(receiverFunction.apply(message.receiverId), message);
                                    }
                                }
                            }
                    );
                }
            });
        }
        else {
            for (MessageWrapper message : concat(receiverLists)) {
                pushMessageToReceiver(receiverFunction.apply(message.receiverId), message);
            }
        }

        clear();
    }

    private static void pushMessageToReceiver(MessageReceiver receiver, ACLMessage message) {
        assert receiver != null;
        receiver.pushMessage(message);
    }

    /**
     *
     *
     * @param message the message
     * @return the number of deliveries
     */
    public synchronized int dispatch(ACLMessage message) {
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
        if (receiverLists.get(bin).isEmpty())
            return ImmutableList.of();

        final List<ACLMessage> ret = Lists.newArrayList();
        final List<MessageWrapper> messages = receiverLists.get(bin);
        Iterator<MessageWrapper> iterator = messages.listIterator();
        while (iterator.hasNext()) {
            MessageWrapper messageWrapper = iterator.next();
            if (messageWrapper.receiverId == receiverId) {
                ret.add(messageWrapper);
                iterator.remove();
            }
        }
        messageCounter -= ret.size();
        return ret;
    }

    private List<ACLMessage> pollMessagesInBin(final int bin, final MessageTemplate messageTemplate) {
        assert messageTemplate != null;

        if (messageTemplate.equals(MessageTemplates.alwaysFalse()))
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
        return id & (BINS-1);
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
        return unmodifiableIterator(Iterables.<ACLMessage>concat(receiverLists).iterator());
    }

    private final class MessageWrapper extends ForwardingACLMessage {
        private final int receiverId;
        private final ACLMessage message;

        private MessageWrapper(ACLMessage message, int receiverId) {
            this.message = message;
            this.receiverId = receiverId;
        }

        @Override
        protected ACLMessage delegate() {
            return message;
        }

        @Override
        public List<Integer> getRecipients() {
            return ImmutableList.of(receiverId);
        }
    }
}
