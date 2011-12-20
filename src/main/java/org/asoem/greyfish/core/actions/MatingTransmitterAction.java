package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@ClassGroup(tags="actions")
public class MatingTransmitterAction extends ContractNetParticipantAction {

    @Element(name="messageType", required=false)
    private String ontology;

    @Element(name="spermFitnessExpression", required = false)
    private GreyfishExpression spermFitnessExpression =
            GreyfishExpressionFactory.compile("0");

    @SimpleXMLConstructor
    private MatingTransmitterAction() {
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
                spermFitnessExpression = GreyfishExpressionFactory.compile(arg0.getExpression());
            }

            @Override
            public GreyfishExpression get() {
                return spermFitnessExpression;
            }
        });
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message) {
        final Genome<Gene<?>> sperm = agent().createGamete();

        double fitness = 0.0;
        try {
            fitness = spermFitnessExpression.evaluateAsDouble(this);
        } catch (EvaluationException e) {
            LoggerFactory.getLogger(MatingTransmitterAction.class).error("Evaluation failed", e);
        }

        return ImmutableACLMessage.<Agent>createReply(message, agent())
                .content(new EvaluatedGenome<Gene<?>>(sperm, fitness), EvaluatedGenome.class)
                .performative(ACLPerformative.PROPOSE);
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleAccept(ACLMessage<Agent> message) {
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
        this.spermFitnessExpression = cloneable.spermFitnessExpression;
    }

    protected MatingTransmitterAction(AbstractBuilder<? extends MatingTransmitterAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.spermFitnessExpression = builder.spermFitnessExpression;
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<MatingTransmitterAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public MatingTransmitterAction checkedBuild() {
            return new MatingTransmitterAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends MatingTransmitterAction, T extends AbstractBuilder<E, T>> extends ContractNetParticipantAction.AbstractBuilder<E, T> {
        private String ontology;
        private GreyfishExpression spermFitnessExpression =
                GreyfishExpressionFactory.compile("0.0");

        public T spermFitnessExpression(String spermFitnessExpression) { this.spermFitnessExpression = GreyfishExpressionFactory.compile(spermFitnessExpression); return self(); }
        public T classification(String ontology) { this.ontology = checkNotNull(ontology); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(!Strings.isNullOrEmpty(ontology));
        }
    }
}
