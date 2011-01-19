package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

@ClassGroup(tags="actions")
public class MatingTransmitterAction extends ContractNetResponderAction {

    @Element(name="messageType", required=false)
    private String parameterMessageType;

    private MatingTransmitterAction() {
        this(new Builder());
    }

    @Override
    protected String getOntology() {
        return parameterMessageType;
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        checkValidity();
    }

    private void checkValidity() {
        Preconditions.checkNotNull(parameterMessageType);
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField(new ValueAdaptor<String>("Message Type", String.class, parameterMessageType) {

            @Override
            protected void writeThrough(String arg0) {
                MatingTransmitterAction.this.parameterMessageType = arg0;
            }
        });
    }

    @Override
    protected ACLMessage handleAccept(ACLMessage message) {
        ACLMessage reply = message.createReply();
        reply.setPerformative(ACLPerformative.INFORM);
        return reply;
    }

    @Override
    protected ACLMessage handleCFP(ACLMessage message) {
        final Genome sperm = new Genome(componentOwner.getGenome());
        sperm.mutate();

        final ACLMessage reply = message.createReply();
        reply.setReferenceContent(new EvaluatedGenome(sperm, evaluateFormula()));
        reply.setPerformative(ACLPerformative.PROPOSE);
        return reply;
    }

    protected MatingTransmitterAction(AbstractBuilder<?> builder) {
        super(builder);
        this.parameterMessageType = builder.parameterMessageType;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetResponderAction.AbstractBuilder<T> {
        private String parameterMessageType;

        public T parameterMessageType(String parameterMessageType) { this.parameterMessageType = parameterMessageType; return self(); }

        protected T fromClone(MatingTransmitterAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).parameterMessageType(action.parameterMessageType);
            return self();
        }

        public MatingTransmitterAction build() { return new MatingTransmitterAction(this); }
    }
}
