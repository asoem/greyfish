package org.asoem.sico.core.actions;

import java.util.Map;

import javolution.util.FastMap;

import org.asoem.sico.core.acl.ACLMessage;
import org.asoem.sico.core.acl.ACLPerformative;
import org.asoem.sico.core.acl.MessageTemplate;
import org.asoem.sico.core.properties.ResourceProperty;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ValueAdaptor;
import org.asoem.sico.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

@ClassGroup(tags="action")
public class ResourceProvisionAction extends ContractNetResponderAction {

	@Element(name="resource")
	private ResourceProperty resourceProperty;

	@Element(name="messageType", required=false)
	private String parameterMessageType;

	private final FastMap<Integer, Double> conversationIdOfferMap = new FastMap<Integer, Double>();

	public ResourceProvisionAction() {
		// TODO Auto-generated constructor stub
	}	

	@Override
	protected ACLMessage handleAccept(ACLMessage message) {
		resourceProperty.substract(conversationIdOfferMap.get(message.getConversationId()));
		ACLMessage reply = message.createReply();
		reply.setPerformative(ACLPerformative.INFORM);
		return reply;
	}

	protected ResourceProvisionAction(ResourceProvisionAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		resourceProperty = deepClone(action.resourceProperty, mapDict);
		parameterMessageType = action.parameterMessageType;
	}

	@Override
	protected ACLMessage handleCFP(ACLMessage message) {
		String amountRequestedStr = message.getContent();
		Double amountRequested = Double.valueOf(amountRequestedStr);

		Double offer = Math.min(amountRequested, resourceProperty.getValue());
		resourceProperty.substract(offer);
		conversationIdOfferMap.put(message.getConversationId(), offer);

		final ACLMessage reply = message.createReply();
		reply.setContent(String.valueOf(offer));
		reply.setPerformative(ACLPerformative.PROPOSE);
		return reply;
	}

	@Override
	protected MessageTemplate createCFPTemplate() {
		return MessageTemplate.ontology(parameterMessageType);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ResourceProvisionAction(this, mapDict);
	}

	@Override
	protected void handleReject(ACLMessage message) {
		resourceProperty.add(conversationIdOfferMap.get(message.getConversationId()));
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField(new ValueAdaptor<String>("Ontology", String.class, parameterMessageType) {
			@Override protected void writeThrough(String arg0) {
				parameterMessageType = arg0;
			}
		});
		e.addField(new ValueSelectionAdaptor<ResourceProperty>("Ontology", ResourceProperty.class, resourceProperty, componentOwner.getProperties(ResourceProperty.class)) {
			@Override protected void writeThrough(ResourceProperty arg0) {
				resourceProperty = arg0;
			}
		});
	}
}
