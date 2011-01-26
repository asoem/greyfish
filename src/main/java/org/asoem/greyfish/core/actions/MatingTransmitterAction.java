package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="actions")
public class MatingTransmitterAction extends ContractNetResponderAction {

    @Element(name="messageType", required=false)
    private String ontology;

    private MatingTransmitterAction() {
        this(new Builder());
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        checkValidity();
    }

    private void checkValidity() {
        Preconditions.checkNotNull(ontology);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField(new ValueAdaptor<String>("Message Type", String.class, ontology) {

            @Override
            protected void writeThrough(String arg0) {
                ontology = checkFrozen(checkNotNull(arg0));
            }
        });
    }

    @Override
    protected ACLMessage.Builder handleAccept(ACLMessage message) {
        return message.replyFrom(componentOwner)
                .performative(ACLPerformative.INFORM);
    }

    @Override
    protected ACLMessage.Builder handleCFP(ACLMessage message) {
        final Genome sperm = new Genome(componentOwner.getGenome());
        sperm.mutate();

        return message.replyFrom(componentOwner)
                .objectContent(new EvaluatedGenome(sperm, evaluateFormula()))
                .performative(ACLPerformative.PROPOSE);
    }

    @Override
    protected MatingTransmitterAction deepCloneHelper(CloneMap cloneMap) {
        return new MatingTransmitterAction(this, cloneMap);
    }

    private MatingTransmitterAction(MatingTransmitterAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        this.ontology = cloneable.ontology;
    }

    protected MatingTransmitterAction(AbstractBuilder<?> builder) {
        super(builder);
        this.ontology = builder.ontology;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<MatingTransmitterAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public MatingTransmitterAction build() { return new MatingTransmitterAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetResponderAction.AbstractBuilder<T> {
        private String ontology;

        public T offersSpermToMatesOfType(String ontology) { this.ontology = checkNotNull(ontology); return self(); }
    }
}
