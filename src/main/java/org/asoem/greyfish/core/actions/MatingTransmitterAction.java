package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.EvaluatedGenome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.ImmutableGenome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@ClassGroup(tags="actions")
public class MatingTransmitterAction extends ContractNetParticipantAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatingTransmitterAction.class);

    @Element(name="ontology", required=false)
    private String ontology;

    @Element(name="spermFitness", required = false)
    private GreyfishExpression spermFitness;

    @Element(name="matingProbability", required = false)
    private GreyfishExpression matingProbability;

    @SimpleXMLConstructor
    public MatingTransmitterAction() {
        this(new Builder());
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        checkValidity();
    }

    private void checkValidity() {
        Preconditions.checkNotNull(ontology);
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
        e.add("Sperm Fitness", new AbstractTypedValueModel<GreyfishExpression>() {

            @Override
            protected void set(GreyfishExpression arg0) {
                spermFitness = GreyfishExpressionFactoryHolder.compile(arg0.getExpression());
            }

            @Override
            public GreyfishExpression get() {
                return spermFitness;
            }
        });
        e.add("matingProbability", new AbstractTypedValueModel<GreyfishExpression>() {
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

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message, Simulation simulation) {
        final ImmutableACLMessage.Builder<Agent> reply = ImmutableACLMessage.createReply(message, agent());

        final double probability = matingProbability.evaluateForContext(this, "mate", message.getSender()).asDouble();
        if (RandomUtils.trueWithProbability(probability)) {
            final Genome<Gene<?>> sperm = ImmutableGenome.copyOf(agent().getGenome());
            double fitness = 0.0;
            try {
                fitness = spermFitness.evaluateForContext(this).asDouble();
            } catch (EvaluationException e) {
                LOGGER.error("Evaluation of spermFitness failed: {}", spermFitness, e);
            }
            reply.content(new EvaluatedGenome<Gene<?>>(sperm, fitness), EvaluatedGenome.class)
                    .performative(ACLPerformative.PROPOSE);
            LOGGER.debug("Accepted mating with p={}", probability);
        }
        else {
            reply.performative(ACLPerformative.REFUSE);
            LOGGER.debug("Refused mating with p={}", probability);
        }

        return reply;
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleAccept(ACLMessage<Agent> message, Simulation simulation) {
        // costs for mating define quality of the genome
//        DoubleProperty doubleProperty = null;
//        Genome sperm = null;
//        doubleProperty.subtract(spermEvaluationFunction.parallelApply(sperm));

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
        this.spermFitness = cloneable.spermFitness;
        this.matingProbability = cloneable.matingProbability;
    }

    protected MatingTransmitterAction(AbstractBuilder<? extends MatingTransmitterAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.spermFitness = builder.spermFitnessExpression;
        this.matingProbability = builder.matingProbabilityExpression;
    }

    public static Builder with() { return new Builder(); }

    public GreyfishExpression getMatingProbability() {
        return matingProbability;
    }

    public GreyfishExpression getSpermFitness() {
        return spermFitness;
    }

    public static final class Builder extends AbstractBuilder<MatingTransmitterAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public MatingTransmitterAction checkedBuild() {
            return new MatingTransmitterAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends MatingTransmitterAction, T extends AbstractBuilder<E, T>> extends ContractNetParticipantAction.AbstractBuilder<E, T> {
        private String ontology = "mate";
        private GreyfishExpression spermFitnessExpression =
                GreyfishExpressionFactoryHolder.compile("0.0");
        public GreyfishExpression matingProbabilityExpression =
                GreyfishExpressionFactoryHolder.compile("1.0");

        public T matingProbability(GreyfishExpression matingProbabilityExpression) { this.matingProbabilityExpression = checkNotNull(matingProbabilityExpression); return self(); }
        public T spermFitness(GreyfishExpression spermFitnessExpression) { this.spermFitnessExpression = checkNotNull(spermFitnessExpression); return self(); }
        public T ontology(String ontology) { this.ontology = checkNotNull(ontology); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(!Strings.isNullOrEmpty(ontology));
        }
    }
}
