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

	public MatingTransmitterAction() {
	}

	public MatingTransmitterAction(String name) {
		super(name);
	}

    @Override
    protected String getOntology() {
        return parameterMessageType;
    }

    public MatingTransmitterAction(String name, String parameterMessageType) {
		super(name);
		this.parameterMessageType = parameterMessageType;
	}

	protected MatingTransmitterAction(
			MatingTransmitterAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		parameterMessageType = action.parameterMessageType;
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
		return new MatingTransmitterAction(this, mapDict);
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
}
