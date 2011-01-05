package org.asoem.sico.core.actions;

import java.util.Map;

import org.asoem.sico.core.acl.ACLMessage;
import org.asoem.sico.core.acl.ACLPerformative;
import org.asoem.sico.core.acl.MessageTemplate;
import org.asoem.sico.core.genes.Genome;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import com.google.common.base.Preconditions;

@ClassGroup(tags="action")
public class MatingTransmitterAction extends ContractNetResponderAction {

	private static final long serialVersionUID = -1990456980403718718L;

	@Element(name="messageType", required=false)
	private String parameterMessageType;

	public MatingTransmitterAction() {
	}

	public MatingTransmitterAction(String name) {
		super(name);
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
		e.addField( new ValueAdaptor<String>("Message Type", String.class, parameterMessageType) {

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

	@Override
	protected MessageTemplate createCFPTemplate() {
		return MessageTemplate.ontology(parameterMessageType);
	}
}
