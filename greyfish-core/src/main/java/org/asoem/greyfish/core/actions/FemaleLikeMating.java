/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static org.asoem.greyfish.utils.base.Callbacks.call;

/**
 *
 */
public final class FemaleLikeMating<A extends SpatialAgent<A, ?, ?, ?>> extends ContractNetInitiatorAction<A> {

    private static final Logger logger = LoggerFactory.getLogger(FemaleLikeMating.class);

    private String ontology;

    private Callback<? super FemaleLikeMating<A>, Double> interactionRadius;

    private Callback<? super FemaleLikeMating<A>, Double> matingProbability;

    private List<A> sensedMates = ImmutableList.of();

    private List<Chromosome> receivedSperm = Lists.newArrayList();

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public FemaleLikeMating() {
        this(new Builder<A>());
    }

    protected FemaleLikeMating(final AbstractBuilder<A, ? extends FemaleLikeMating<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.interactionRadius = builder.sensorRange;
        this.matingProbability = builder.matingProbability;
    }

    private void receiveSperm(final Chromosome chromosome, final A sender, final AgentContext<A> context) {
        receivedSperm.add(chromosome);
        //agent().get().logEvent(this, "spermReceived", String.valueOf(sender.getId()));
        logger.debug(context.agent() + " received sperm: " + chromosome);
    }

    @Override
    protected ImmutableACLMessage.Builder<A> createCFP(final AgentContext<A> context) {
        final int sensedMatesCount = Iterables.size(sensedMates);
        assert (sensedMatesCount > 0); // see #evaluateCondition(Simulation)

        final A receiver = Iterables.get(sensedMates, RandomGenerators.rng().nextInt(sensedMatesCount));
        sensedMates = ImmutableList.of();

        return ImmutableACLMessage.<A>builder()
                .sender(context.agent())
                .performative(ACLPerformative.CFP)
                .ontology(ontology)
                        // Choose randomly one receiver. Adding evaluates possible candidates as receivers will decrease the performance in high density populations!
                .addReceiver(receiver);
    }

    @Override
    protected ImmutableACLMessage.Builder<A> handlePropose(final ACLMessage<A> message, final AgentContext<A> context) {
        final ImmutableACLMessage.Builder<A> builder = ImmutableACLMessage.createReply(message, context.agent());
        final Object messageContent = message.getContent();
        if (!(messageContent instanceof Chromosome)) {
            throw new NotUnderstoodException("Payload of message is not of type Chromosome: " + messageContent);
        }

        final Chromosome chromosome = (Chromosome) messageContent;
        final double probability = matingProbability.apply(this, ImmutableMap.of("mate", message.getSender()));
        if (RandomGenerators.nextBoolean(RandomGenerators.rng(), probability)) {
            receiveSperm(chromosome, message.getSender(), context);
            builder.performative(ACLPerformative.ACCEPT_PROPOSAL);
            logger.debug("Accepted mating with p={}", probability);
        } else {
            builder.performative(ACLPerformative.REJECT_PROPOSAL);
            logger.debug("Refused mating with p={}", probability);
        }

        return builder;
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    public List<Chromosome> getReceivedSperm() {
        return receivedSperm;
    }

    /**
     * The the number of successful matings, equivalent to the size of {@link #getReceivedSperm()}
     *
     * @return the the number of successful matings
     */
    public int successfulMatings() {
        return receivedSperm.size();
    }

    @Override
    public void initialize() {
        super.initialize();
        receivedSperm.clear();
    }

    @Override
    protected boolean canInitiate(final AgentContext<A> context) {
        sensedMates = ImmutableList.copyOf(context.agent().findNeighbours(call(interactionRadius, this)));
        return !isEmpty(sensedMates);
    }

    public static <A extends SpatialAgent<A, ?, ?, ?>> Builder<A> with() {
        return new Builder<A>();
    }

    public Callback<? super FemaleLikeMating<A>, Double> getMatingProbability() {
        return matingProbability;
    }

    public Callback<? super FemaleLikeMating<A>, Double> getInteractionRadius() {
        return interactionRadius;
    }

    public static final class Builder<A extends SpatialAgent<A, ?, ?, ?>> extends AbstractBuilder<A, FemaleLikeMating<A>, Builder<A>> {
        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected FemaleLikeMating<A> checkedBuild() {
            return new FemaleLikeMating<A>(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends SpatialAgent<A, ?, ?, ?>, C extends FemaleLikeMating<A>, B extends AbstractBuilder<A, C, B>> extends ContractNetInitiatorAction.AbstractBuilder<A, C, B> {
        protected String ontology = "mate";
        protected Callback<? super FemaleLikeMating<A>, Double> sensorRange = Callbacks.constant(1.0);
        protected Callback<? super FemaleLikeMating<A>, Double> matingProbability = Callbacks.constant(1.0);

        /**
         * Set the callback function will determine the mating probability. The possible mate ({@code Agent }) is passed
         * as an argument to the callback with key "mate"
         *
         * @param callback the callback function to calculate the mating probability
         * @return this builder
         */
        public final B matingProbability(final Callback<? super FemaleLikeMating<A>, Double> callback) {
            this.matingProbability = checkNotNull(callback);
            return self();
        }

        public final B ontology(final String ontology) {
            this.ontology = checkNotNull(ontology);
            return self();
        }

        public final B interactionRadius(final Callback<? super FemaleLikeMating<A>, Double> callback) {
            this.sensorRange = callback;
            return self();
        }

        @Override
        protected final void checkBuilder() {
            super.checkBuilder();
            if (Strings.isNullOrEmpty(ontology))
                logger.warn(FemaleLikeMating.class.getSimpleName() + ": ontology is invalid '" + ontology + "'");
        }
    }
}
