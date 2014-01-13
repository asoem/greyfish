package org.asoem.greyfish.core.actions;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.Tagged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.utils.math.RandomGenerators.nextBoolean;
import static org.asoem.greyfish.utils.math.RandomGenerators.rng;

@Tagged("actions")
public class MaleLikeMating<A extends SpatialAgent<A, ?, ?, ?>> extends ContractNetParticipantAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaleLikeMating.class);

    private String ontology;
    private Callback<? super MaleLikeMating<A>, Double> matingProbability;
    private int matingCount;
    private boolean proposalSent;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private MaleLikeMating() {
        this(new Builder<A>());
    }

    private MaleLikeMating(final AbstractBuilder<A, ? extends MaleLikeMating<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
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
    protected ImmutableACLMessage.Builder<A> handleCFP(final ACLMessage<A> message, final AgentContext<A> context) {
        final ImmutableACLMessage.Builder<A> reply = ImmutableACLMessage.createReply(message, context.agent());

        if (proposalSent) // TODO: CFP messages are not randomized. Problem?
            return reply.performative(ACLPerformative.REFUSE);

        final double probability = matingProbability.apply(this, ImmutableMap.of("mate", message.getSender()));
        if (nextBoolean(rng(), probability)) {
            final Chromosome chromosome = null;//HeritableTraitsChromosome.copyFromAgent(context.agent());
            reply.content(chromosome)
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
    protected ImmutableACLMessage.Builder<A> handleAccept(final ACLMessage<A> message, final AgentContext<A> context) {
        // costs for mating define quality of the agentTraitList
//        DoubleProperty doubleProperty = null;
//        GeneComponentList sperm = null;
//        doubleProperty.subtract(spermEvaluationFunction.evaluate(sperm));
        ++matingCount;
        return ImmutableACLMessage.createReply(message, context.agent())
                .performative(ACLPerformative.INFORM);
    }

    public static <A extends SpatialAgent<A, ?, ?, ?>> Builder<A> with() {
        return new Builder<A>();
    }

    public Callback<? super MaleLikeMating<A>, Double> getMatingProbability() {
        return matingProbability;
    }

    public int getMatingCount() {
        return matingCount;
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static final class Builder<A extends SpatialAgent<A, ?, ?, ?>> extends AbstractBuilder<A, MaleLikeMating<A>, Builder<A>> {
        private Builder(final MaleLikeMating<A> maleLikeMating) {
            super(maleLikeMating);
        }

        private Builder() {
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        public MaleLikeMating<A> checkedBuild() {
            return new MaleLikeMating<A>(this);
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

    protected static abstract class AbstractBuilder<A extends SpatialAgent<A, ?, ?, ?>, C extends MaleLikeMating<A>, B extends AbstractBuilder<A, C, B>> extends ContractNetParticipantAction.AbstractBuilder<A, C, B> implements Serializable {
        private String ontology = "mate";
        private Callback<? super MaleLikeMating<A>, Double> matingProbabilityExpression = Callbacks.constant(1.0);
        private int matingCount;
        private boolean proposalSent;

        protected AbstractBuilder(final MaleLikeMating<A> maleLikeMating) {
            super(maleLikeMating);
            this.ontology = maleLikeMating.ontology;
            this.matingProbabilityExpression = maleLikeMating.matingProbability;
            this.matingCount = maleLikeMating.matingCount;
            this.proposalSent = maleLikeMating.proposalSent;
        }

        protected AbstractBuilder() {
        }

        public B matingProbability(final Callback<? super MaleLikeMating<A>, Double> matingProbability) {
            this.matingProbabilityExpression = checkNotNull(matingProbability);
            return self();
        }

        public B ontology(final String ontology) {
            this.ontology = checkNotNull(ontology);
            return self();
        }

        @Override
        protected void checkBuilder() {
            super.checkBuilder();
            checkState(!Strings.isNullOrEmpty(ontology));
        }
    }
}
