package org.asoem.greyfish.core.actions;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.ChromosomeImpl;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Tagged("actions")
public class MaleLikeMating extends ContractNetParticipantAction {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(MaleLikeMating.class);

    private String ontology;
    private Callback<? super MaleLikeMating, Double> matingProbability;
    private int matingCount;
    private boolean proposalSent;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private MaleLikeMating() {
        this(new Builder());
    }

    private MaleLikeMating(MaleLikeMating cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.ontology = cloneable.ontology;
        this.matingProbability = cloneable.matingProbability;
        this.matingCount = cloneable.matingCount;
        this.proposalSent = cloneable.proposalSent;
    }

    private MaleLikeMating(AbstractBuilder<? extends MaleLikeMating, ? extends AbstractBuilder> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.matingProbability = builder.matingProbabilityExpression;
        this.matingCount = builder.matingCount;
        this.proposalSent = builder.proposalSent;
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    protected void prepareForCommunication() {
        proposalSent = false;
    }

    @Override
    public void initialize() {
        super.initialize();
        matingCount = 0;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Ontology", TypedValueModels.forField("ontology", this, String.class));
        e.add("Mating Probability", TypedValueModels.forField("matingProbability", this, new TypeToken<Callback<? super MaleLikeMating, Double>>() {
        }));
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message, Simulation simulation) {
        final ImmutableACLMessage.Builder<Agent> reply = ImmutableACLMessage.createReply(message, agent());

        if (proposalSent) // TODO: CFP messages are not randomized. Problem?
            return reply.performative(ACLPerformative.REFUSE);

        final double probability = matingProbability.apply(this, ArgumentMap.of("mate", message.getSender()));
        if (RandomUtils.nextBoolean(probability)) {

            final Chromosome chromosome = ChromosomeImpl.forAgent(agent());
            reply.content(chromosome, Chromosome.class)
                    .performative(ACLPerformative.PROPOSE);

            proposalSent = true;

            LOGGER.debug("Accepted mating with p={}", probability);
        } else {
            reply.performative(ACLPerformative.REFUSE);
            LOGGER.debug("Refused mating with p={}", probability);
        }

        return reply;
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleAccept(ACLMessage<Agent> message, Simulation simulation) {
        // costs for mating define quality of the agentTraitList
//        DoubleProperty doubleProperty = null;
//        GeneComponentList sperm = null;
//        doubleProperty.subtract(spermEvaluationFunction.parallelApply(sperm));
        ++matingCount;
        return ImmutableACLMessage.createReply(message, getAgent())
                .performative(ACLPerformative.INFORM);
    }

    @Override
    public MaleLikeMating deepClone(DeepCloner cloner) {
        return new MaleLikeMating(this, cloner);
    }

    public static Builder with() {
        return new Builder();
    }

    public Callback<? super MaleLikeMating, Double> getMatingProbability() {
        return matingProbability;
    }

    public int getMatingCount() {
        return matingCount;
    }

    private Object writeReplace() {
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static final class Builder extends AbstractBuilder<MaleLikeMating, Builder> {
        private Builder(MaleLikeMating maleLikeMating) {
            super(maleLikeMating);
        }

        private Builder() {}

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public MaleLikeMating checkedBuild() {
            return new MaleLikeMating(this);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }

    protected static abstract class AbstractBuilder<C extends MaleLikeMating, B extends AbstractBuilder<C, B>> extends ContractNetParticipantAction.AbstractBuilder<C, B> implements Serializable {
        private String ontology = "mate";
        private Callback<? super MaleLikeMating, Double> matingProbabilityExpression = Callbacks.constant(1.0);
        private int matingCount;
        private boolean proposalSent;

        protected AbstractBuilder(MaleLikeMating maleLikeMating) {
            super(maleLikeMating);
            this.ontology = maleLikeMating.ontology;
            this.matingProbabilityExpression = maleLikeMating.matingProbability;
            this.matingCount = maleLikeMating.matingCount;
            this.proposalSent = maleLikeMating.proposalSent;
        }

        protected AbstractBuilder() {}

        public B matingProbability(Callback<? super MaleLikeMating, Double> matingProbability) {
            this.matingProbabilityExpression = checkNotNull(matingProbability);
            return self();
        }

        public B ontology(String ontology) {
            this.ontology = checkNotNull(ontology);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(!Strings.isNullOrEmpty(ontology));
        }
    }
}
