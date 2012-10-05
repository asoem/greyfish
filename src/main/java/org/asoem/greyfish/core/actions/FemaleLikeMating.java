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
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.ChromosomeImpl;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.ArgumentMap;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
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
@ClassGroup(tags = "actions")
public class FemaleLikeMating extends ContractNetInitiatorAction {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(FemaleLikeMating.class);

    @Element(name = "ontology", required = false)
    private String ontology;

    @Element(name = "interactionRadius", required = false)
    private Callback<? super FemaleLikeMating, Double> interactionRadius;

    @Element(name = "matingProbability", required = false)
    private Callback<? super FemaleLikeMating, Double> matingProbability;

    private List<Agent> sensedMates = ImmutableList.of();

    private List<Chromosome> receivedSperm = Lists.newArrayList();

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public FemaleLikeMating() {
        this(new Builder());
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Ontology", forField("ontology", this, String.class));
        e.add("Interaction Radius", forField("interactionRadius", this, Callback.class));
        e.add("Mating Probability", forField("matingProbability", this, Callback.class));
    }

    private void receiveSperm(Chromosome chromosome, Agent sender, Simulation simulation) {
        receivedSperm.add(chromosome);
        agent().logEvent(this, "spermReceived", String.valueOf(sender.getId()));
        LOGGER.info(getAgent() + " received sperm: " + chromosome);
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> createCFP(Simulation simulation) {
        final int sensedMatesCount = Iterables.size(sensedMates);
        assert (sensedMatesCount > 0); // see #evaluateCondition(Simulation)

        final Agent receiver = Iterables.get(sensedMates, RandomUtils.nextInt(sensedMatesCount));
        sensedMates = ImmutableList.of();

        return ImmutableACLMessage.<Agent>with()
                .sender(agent())
                .performative(ACLPerformative.CFP)
                .ontology(ontology)
                        // Choose randomly one receiver. Adding evaluates possible candidates as receivers will decrease the performance in high density populations!
                .setReceivers(receiver);
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handlePropose(ACLMessage<Agent> message, Simulation simulation) throws NotUnderstoodException {
        final ImmutableACLMessage.Builder<Agent> builder = ImmutableACLMessage.createReply(message, agent());
        try {
            Chromosome chromosome = message.getContent(ChromosomeImpl.class);
            final double probability = matingProbability.apply(this, ArgumentMap.of("mate", message.getSender()));
            if (RandomUtils.nextBoolean(probability)) {
                receiveSperm(chromosome, message.getSender(), simulation);
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
    protected boolean canInitiate(Simulation simulation) {
        sensedMates = ImmutableList.copyOf(simulation.findNeighbours(agent(), call(interactionRadius, this)));
        return !isEmpty(sensedMates);
    }

    @Override
    public FemaleLikeMating deepClone(DeepCloner cloner) {
        return new FemaleLikeMating(this, cloner);
    }

    private FemaleLikeMating(FemaleLikeMating cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.ontology = cloneable.ontology;
        this.interactionRadius = cloneable.interactionRadius;
        this.matingProbability = cloneable.matingProbability;
    }

    protected FemaleLikeMating(AbstractBuilder<? extends FemaleLikeMating, ? extends AbstractBuilder> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.interactionRadius = builder.sensorRange;
        this.matingProbability = builder.matingProbability;
    }

    public static Builder with() {
        return new Builder();
    }

    public Callback<? super FemaleLikeMating, Double> getMatingProbability() {
        return matingProbability;
    }

    public Callback<? super FemaleLikeMating, Double> getInteractionRadius() {
        return interactionRadius;
    }

    public static final class Builder extends AbstractBuilder<FemaleLikeMating, Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected FemaleLikeMating checkedBuild() {
            return new FemaleLikeMating(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<C extends FemaleLikeMating, B extends AbstractBuilder<C, B>> extends AbstractGFAction.AbstractBuilder<C, B> {
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
