package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.genes.UniparentalChromosomalHistory;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Callback;
import org.asoem.greyfish.core.individual.Callbacks;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@ClassGroup(tags = "actions")
public class MatingTransmitterAction extends ContractNetParticipantAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatingTransmitterAction.class);

    @Element(name = "ontology", required = false)
    private String ontology;

    @Element(name = "matingProbability", required = false)
    private Callback<? super MatingTransmitterAction, Double> matingProbability;

    private int matingCount;

    private boolean proposalSent;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public MatingTransmitterAction() {
        this(new Builder());
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
        e.add("Mating Probability", TypedValueModels.forField("matingProbability", this, new TypeToken<Callback<? super MatingTransmitterAction, Double>>() {
        }));
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message, Simulation simulation) {
        final ImmutableACLMessage.Builder<Agent> reply = ImmutableACLMessage.createReply(message, agent());

        if (proposalSent) // TODO: CFP messages are not randomized. Problem?
            return reply.performative(ACLPerformative.REFUSE);

        final double probability = matingProbability.apply(this, ImmutableMap.of("mate", message.getSender()));
        if (RandomUtils.trueWithProbability(probability)) {

            final Chromosome chromosome = new Chromosome(
                    new UniparentalChromosomalHistory(agent().getId()),
                    Iterables.transform(agent().getGeneComponentList(), new Function<GeneComponent<?>, Gene<?>>() {
                        @Override
                        public Gene<?> apply(@Nullable GeneComponent<?> geneComponent) {
                            assert geneComponent != null;
                            return new Gene<Object>(geneComponent.mutatedValue(), geneComponent.getRecombinationProbability());
                        }
                    }));


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
        // costs for mating define quality of the geneComponentList
//        DoubleProperty doubleProperty = null;
//        GeneComponentList sperm = null;
//        doubleProperty.subtract(spermEvaluationFunction.parallelApply(sperm));
        ++matingCount;
        return ImmutableACLMessage.createReply(message, getAgent())
                .performative(ACLPerformative.INFORM);
    }

    @Override
    public MatingTransmitterAction deepClone(DeepCloner cloner) {
        return new MatingTransmitterAction(this, cloner);
    }

    private MatingTransmitterAction(MatingTransmitterAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.ontology = cloneable.ontology;
        this.matingProbability = cloneable.matingProbability;
    }

    protected MatingTransmitterAction(AbstractBuilder<? extends MatingTransmitterAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.matingProbability = builder.matingProbabilityExpression;
    }

    public static Builder with() {
        return new Builder();
    }

    public Callback<? super MatingTransmitterAction, Double> getMatingProbability() {
        return matingProbability;
    }

    public int getMatingCount() {
        return matingCount;
    }

    public static final class Builder extends AbstractBuilder<MatingTransmitterAction, Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public MatingTransmitterAction checkedBuild() {
            return new MatingTransmitterAction(this);
        }
    }

    protected static abstract class AbstractBuilder<E extends MatingTransmitterAction, T extends AbstractBuilder<E, T>> extends AbstractActionBuilder<E, T> {
        private String ontology = "mate";
        public Callback<? super MatingTransmitterAction, Double> matingProbabilityExpression = Callbacks.constant(1.0);

        public T matingProbability(Callback<? super MatingTransmitterAction, Double> matingProbability) {
            this.matingProbabilityExpression = checkNotNull(matingProbability);
            return self();
        }

        public T ontology(String ontology) {
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
