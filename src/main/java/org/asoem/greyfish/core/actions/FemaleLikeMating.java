/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.ChromosomeImpl;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static org.asoem.greyfish.utils.base.Callbacks.call;
import static org.asoem.greyfish.utils.gui.TypedValueModels.forField;

/**
 * @author christoph
 */
@Tagged("actions")
public class FemaleLikeMating<A extends SpatialAgent<A, ?, ?>> extends ContractNetInitiatorAction<A> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(FemaleLikeMating.class);

    @Element(name = "ontology", required = false)
    private String ontology;

    @Element(name = "interactionRadius", required = false)
    private Callback<? super FemaleLikeMating, Double> interactionRadius;

    @Element(name = "matingProbability", required = false)
    private Callback<? super FemaleLikeMating, Double> matingProbability;

    private List<A> sensedMates = ImmutableList.of();

    private List<Chromosome> receivedSperm = Lists.newArrayList();

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public FemaleLikeMating() {
        this(new Builder<A>());
    }

    private FemaleLikeMating(FemaleLikeMating<A> cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.ontology = cloneable.ontology;
        this.interactionRadius = cloneable.interactionRadius;
        this.matingProbability = cloneable.matingProbability;
    }

    protected FemaleLikeMating(AbstractBuilder<A, ? extends FemaleLikeMating<A>, ? extends AbstractBuilder<A, ?,?>> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.interactionRadius = builder.sensorRange;
        this.matingProbability = builder.matingProbability;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Ontology", forField("ontology", this, String.class));
        e.add("Interaction Radius", forField("interactionRadius", this, Callback.class));
        e.add("Mating Probability", forField("matingProbability", this, Callback.class));
    }

    private void receiveSperm(Chromosome chromosome, A sender) {
        receivedSperm.add(chromosome);
        agent().logEvent(this, "spermReceived", String.valueOf(sender.getId()));
        LOGGER.info(getAgent() + " received sperm: " + chromosome);
    }

    @Override
    protected ImmutableACLMessage.Builder<A> createCFP() {
        final int sensedMatesCount = Iterables.size(sensedMates);
        assert (sensedMatesCount > 0); // see #evaluateCondition(Simulation)

        final A receiver = Iterables.get(sensedMates, RandomUtils.nextInt(sensedMatesCount));
        sensedMates = ImmutableList.of();

        return ImmutableACLMessage.<A>with()
                .sender(agent())
                .performative(ACLPerformative.CFP)
                .ontology(ontology)
                        // Choose randomly one receiver. Adding evaluates possible candidates as receivers will decrease the performance in high density populations!
                .setReceivers(receiver);
    }

    @Override
    protected ImmutableACLMessage.Builder<A> handlePropose(ACLMessage<A> message) throws NotUnderstoodException {
        final ImmutableACLMessage.Builder<A> builder = ImmutableACLMessage.createReply(message, agent());
        try {
            Chromosome chromosome = message.getContent(ChromosomeImpl.class);
            final double probability = matingProbability.apply(this, ArgumentMap.of("mate", message.getSender()));
            if (RandomUtils.nextBoolean(probability)) {
                receiveSperm(chromosome, message.getSender());
                builder.performative(ACLPerformative.ACCEPT_PROPOSAL);
                LOGGER.info("Accepted mating with p={}", probability);
            } else {
                builder.performative(ACLPerformative.REJECT_PROPOSAL);
                LOGGER.info("Refused mating with p={}", probability);
            }
        } catch (ClassCastException e) {
            throw new NotUnderstoodException("Payload of message is not of type Chromosome: " + message.getContentClass(), e);
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
    protected boolean canInitiate() {
        sensedMates = ImmutableList.copyOf(agent().findNeighbours(call(interactionRadius, this)));
        return !isEmpty(sensedMates);
    }

    @Override
    public FemaleLikeMating<A> deepClone(DeepCloner cloner) {
        return new FemaleLikeMating<A>(this, cloner);
    }

    public static <A extends SpatialAgent<A, ?, ?>> Builder<A> with() {
        return new Builder<A>();
    }

    public Callback<? super FemaleLikeMating, Double> getMatingProbability() {
        return matingProbability;
    }

    public Callback<? super FemaleLikeMating, Double> getInteractionRadius() {
        return interactionRadius;
    }

    public static final class Builder<A extends SpatialAgent<A, ?, ?>> extends AbstractBuilder<A, FemaleLikeMating<A>, Builder<A>> {
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
    protected static abstract class AbstractBuilder<A extends SpatialAgent<A, ?, ?>, C extends FemaleLikeMating<A>, B extends AbstractBuilder<A, C, B>> extends ContractNetInitiatorAction.AbstractBuilder<A, C, B> {
        protected String ontology = "mate";
        protected Callback<? super FemaleLikeMating, Double> sensorRange = Callbacks.constant(1.0);
        protected Callback<? super FemaleLikeMating, Double> matingProbability = Callbacks.constant(1.0);

        /**
         * Set the callback function will determine the mating probability. The possible mate ({@code Agent }) is passed as an argument ({@link org.asoem.greyfish.utils.base.Arguments}) to the callback with key "mate"
         * @param callback the callback function to calculate the mating probability
         * @return this builder
         */
        public B matingProbability(Callback<? super FemaleLikeMating, Double> callback) {
            this.matingProbability = checkNotNull(callback);
            return self();
        }

        public B ontology(String ontology) {
            this.ontology = checkNotNull(ontology);
            return self();
        }

        public B interactionRadius(Callback<? super FemaleLikeMating, Double> callback) {
            this.sensorRange = callback;
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            if (Strings.isNullOrEmpty(ontology))
                LOGGER.warn(FemaleLikeMating.class.getSimpleName() + ": ontology is invalid '" + ontology + "'");
        }
    }
}
