package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.sourceforge.jeval.EvaluationException;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.eval.GreyfishMathExpression;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.GenomeInterface;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.*;

@ClassGroup(tags="actions")
public class MatingTransmitterAction extends ContractNetParticipantAction {

    @Element(name="messageType", required=false)
    private String ontology;

    @Element(name="spermFitnessExpression", required = false)
    private String spermFitnessExpression;

    private Function<GenomeInterface, Double> spermEvaluationFunction = new Function<GenomeInterface, Double>() {
        @Override
        public Double apply(GenomeInterface genome) {
            try {
                return GreyfishMathExpression.evaluate(spermFitnessExpression, Agent.class.cast(getComponentOwner()));
            } catch (EvaluationException e) {
                LoggerFactory.getLogger(MatingTransmitterAction.class).error("Evaluation failed", e);
                return 0.0;
            }
        }
    };

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
    public void export(Exporter e) {
        super.export(e);
        e.add(new ValueAdaptor<String>("Message Type", String.class) {

            @Override
            protected void set(String arg0) {
                ontology = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public String get() {
                return ontology;
            }
        });
        e.add(new ValueAdaptor<String>("Sperm Fitness", String.class) {

            @Override
            protected void set(String arg0) {
                checkArgument(GreyfishMathExpression.isValidExpression(arg0));
                spermFitnessExpression = arg0;
            }

            @Override
            public String get() {
                return spermFitnessExpression;
            }
        });
    }

    @Override
    protected ACLMessage.Builder handleCFP(ACLMessage message) {
        final Genome sperm = getComponentOwner().getGenome();

        return message.createReplyFrom(getComponentOwner().getId())
                .objectContent(new EvaluatedGenome(sperm, spermEvaluationFunction.apply(sperm)))
                .performative(ACLPerformative.PROPOSE);
    }

    @Override
    protected ACLMessage.Builder handleAccept(ACLMessage message) {
        // costs for mating define quality of the genome
//        DoubleProperty doubleProperty = null;
//        GenomeInterface sperm = null;
//        doubleProperty.subtract(spermEvaluationFunction.apply(sperm));

        return message.createReplyFrom(getComponentOwner().getId())
                .performative(ACLPerformative.INFORM);
    }

    @Override
    public MatingTransmitterAction deepCloneHelper(CloneMap cloneMap) {
        return new MatingTransmitterAction(this, cloneMap);
    }

    private MatingTransmitterAction(MatingTransmitterAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        this.ontology = cloneable.ontology;
        this.spermFitnessExpression = cloneable.spermFitnessExpression;
    }

    protected MatingTransmitterAction(AbstractBuilder<?> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.spermFitnessExpression = builder.spermFitnessExpression;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<MatingTransmitterAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public MatingTransmitterAction build() { return new MatingTransmitterAction(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetParticipantAction.AbstractBuilder<T> {
        private String ontology;
        private String spermFitnessExpression = "0.0";

        public T spermFitnessExpression(String spermFitnessExpression) { this.spermFitnessExpression = spermFitnessExpression; return self(); }
        public T offersSpermToMatesOfType(String ontology) { this.ontology = checkNotNull(ontology); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(!Strings.isNullOrEmpty(ontology));
            checkState(GreyfishMathExpression.isValidExpression(spermFitnessExpression));
        }
    }
}
