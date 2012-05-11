/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="actions")
public class MatingReceiverAction extends ContractNetInitiatorAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatingReceiverAction.class);

    @Element(name="ontology", required=false)
    private String ontology;

    @Element(name="interactionRadius", required=false)
    private double interactionRadius;

    @Element(name="matingProbability", required = false)
    private GreyfishExpression matingProbability;

    private Iterable<Agent> sensedMates;

    private List<Chromosome> receivedSperm = Lists.newArrayList();

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public MatingReceiverAction() {
        this(new Builder());
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Message Type", new AbstractTypedValueModel<String>() {
            @Override
            protected void set(String arg0) {
                ontology = checkNotNull(arg0);
            }

            @Override
            public String get() {
                return ontology;
            }
        });
        e.add("Sensor Range", new AbstractTypedValueModel<Double>() {
            @Override
            protected void set(Double arg0) {
                interactionRadius = checkNotNull(arg0);
            }

            @Override
            public Double get() {
                return interactionRadius;
            }
        });
        e.add("matingProbability", "Expected is an expression that returns a double between 0.0 and 1.0",
                new AbstractTypedValueModel<GreyfishExpression>() {
                    @Override
                    protected void set(GreyfishExpression arg0) {
                        matingProbability = arg0;
                    }

                    @Override
                    public GreyfishExpression get() {
                        return matingProbability;
                    }
                });
    }

    private void receiveSperm(Chromosome chromosome, Agent sender, Simulation simulation) {
        receivedSperm.add(chromosome);
        agent().logEvent(this, "spermReceived", String.valueOf(sender.getId()));
        LOGGER.info(getAgent() + " received sperm: " + chromosome);
    }

    @Override
    public boolean checkPreconditions(Simulation simulation) {
        return super.checkPreconditions(simulation);
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> createCFP(Simulation simulation) {
        final int sensedMatesCount = Iterables.size(sensedMates);
        assert(sensedMatesCount > 0); // see #evaluateCondition(Simulation)

        return ImmutableACLMessage.<Agent>with()
                .sender(agent())
                .performative(ACLPerformative.CFP)
                .ontology(ontology)
                        // Choose randomly one receiver. Adding evaluates possible candidates as receivers will decrease the performance in high density populations!
                .setReceivers(Iterables.get(sensedMates, RandomUtils.nextInt(sensedMatesCount)));
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handlePropose(ACLMessage<Agent> message, Simulation simulation) throws NotUnderstoodException {
        final ImmutableACLMessage.Builder<Agent> builder = ImmutableACLMessage.createReply(message, agent());
        try {
            Chromosome chromosome = message.getContent(Chromosome.class);
            final double probability = matingProbability.evaluateForContext(this, "mate", message.getSender()).asDouble();
            if (RandomUtils.trueWithProbability(probability)) {
                receiveSperm(chromosome, message.getSender(), simulation);
                builder.performative(ACLPerformative.ACCEPT_PROPOSAL);
                LOGGER.info("Accepted mating with p={}", probability);
            }
            else {
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
        sensedMates = simulation.findNeighbours(agent(), interactionRadius);
        LOGGER.debug("Found {} possible mate(s)", Iterables.size(sensedMates));
        return ! Iterables.isEmpty(sensedMates);
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

    protected MatingReceiverAction(AbstractBuilder<?extends MatingReceiverAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.interactionRadius = builder.sensorRange;
        this.matingProbability = builder.matingProbabilityExpression;
    }

    public static Builder with() { return new Builder(); }

    public GreyfishExpression getMatingProbability() {
        return matingProbability;
    }

    public double getInteractionRadius() {
        return interactionRadius;
    }

    public static final class Builder extends AbstractBuilder<MatingReceiverAction, Builder> {
        @Override protected Builder self() { return this; }

        @Override
        protected MatingReceiverAction checkedBuild() {
            return new MatingReceiverAction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends MatingReceiverAction, T extends AbstractBuilder<E,T>> extends ContractNetParticipantAction.AbstractBuilder<E,T> {
        protected String ontology = "mate";
        protected double sensorRange = 1.0;
        protected GreyfishExpression matingProbabilityExpression = GreyfishExpressionFactoryHolder.compile("1.0");

        public T matingProbability(GreyfishExpression matingProbabilityExpression) { this.matingProbabilityExpression = checkNotNull(matingProbabilityExpression); return self(); }
        public T ontology(String ontology) { this.ontology = checkNotNull(ontology); return self(); }
        public T interactionRadius(double sensorRange) { this.sensorRange = sensorRange; return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            if (sensorRange <= 0)
                LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": interactionRadius is <= 0 '" + sensorRange + "'");
            if (Strings.isNullOrEmpty(ontology))
                LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": ontology is invalid '" + ontology + "'");
        }
    }
}
