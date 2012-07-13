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
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Callback;
import org.asoem.greyfish.core.individual.Callbacks;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static org.asoem.greyfish.core.individual.Callbacks.call;
import static org.asoem.greyfish.utils.gui.TypedValueModels.forField;

/**
 * @author christoph
 */
@ClassGroup(tags = "actions")
public class MatingReceiverAction extends ContractNetInitiatorAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatingReceiverAction.class);

    @Element(name = "ontology", required = false)
    private String ontology;

    @Element(name = "interactionRadius", required = false)
    private Callback<? super MatingReceiverAction, Double> interactionRadius;

    @Element(name = "matingProbability", required = false)
    private Callback<? super MatingReceiverAction, Double> matingProbability;

    private List<Agent> sensedMates = ImmutableList.of();

    private List<Chromosome> receivedSperm = Lists.newArrayList();

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public MatingReceiverAction() {
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
            Chromosome chromosome = message.getContent(Chromosome.class);
            final double probability = matingProbability.apply(this, ImmutableMap.of("mate", message.getSender()));
            if (RandomUtils.trueWithProbability(probability)) {
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

    public int getMatingCount() {
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
    public MatingReceiverAction deepClone(DeepCloner cloner) {
        return new MatingReceiverAction(this, cloner);
    }

    private MatingReceiverAction(MatingReceiverAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.ontology = cloneable.ontology;
        this.interactionRadius = cloneable.interactionRadius;
        this.matingProbability = cloneable.matingProbability;
    }

    protected MatingReceiverAction(AbstractBuilder<? extends MatingReceiverAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.interactionRadius = builder.sensorRange;
        this.matingProbability = builder.matingProbability;
    }

    public static Builder with() {
        return new Builder();
    }

    public Callback<? super MatingReceiverAction, Double> getMatingProbability() {
        return matingProbability;
    }

    public Callback<? super MatingReceiverAction, Double> getInteractionRadius() {
        return interactionRadius;
    }

    public static final class Builder extends AbstractBuilder<MatingReceiverAction, Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected MatingReceiverAction checkedBuild() {
            return new MatingReceiverAction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends MatingReceiverAction, T extends AbstractBuilder<E, T>> extends AbstractActionBuilder<E, T> {
        protected String ontology = "mate";
        protected Callback<? super MatingReceiverAction, Double> sensorRange = Callbacks.constant(1.0);
        protected Callback<? super MatingReceiverAction, Double> matingProbability = Callbacks.constant(1.0);

        public T matingProbability(Callback<? super MatingReceiverAction, Double> matingProbabilityExpression) {
            this.matingProbability = checkNotNull(matingProbabilityExpression);
            return self();
        }

        public T ontology(String ontology) {
            this.ontology = checkNotNull(ontology);
            return self();
        }

        public T interactionRadius(Callback<? super MatingReceiverAction, Double> sensorRange) {
            this.sensorRange = sensorRange;
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            if (Strings.isNullOrEmpty(ontology))
                LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": ontology is invalid '" + ontology + "'");
        }
    }
}
