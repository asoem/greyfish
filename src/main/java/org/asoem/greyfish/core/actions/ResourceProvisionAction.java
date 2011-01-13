package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

@ClassGroup(tags="action")
public class ResourceProvisionAction extends ContractNetResponderAction {

	@Element(name="resource")
	private ResourceProperty resourceProperty;

	@Element(name="messageType", required=false)
	private String parameterMessageType;

    private double offer;

	public ResourceProvisionAction() {
	}	

	@Override
	protected ACLMessage handleAccept(ACLMessage message) {
        resourceProperty.subtract(offer);

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
		String amountRequestedStr = message.getStringContent();
        double amountRequested = 0;
        try {
            amountRequested = Double.valueOf(amountRequestedStr).doubleValue();
        } catch (Exception e) {
            GreyfishLogger.error("Unexpected message content", e);
        }
        offer = Math.min(amountRequested, resourceProperty.getValue());

		final ACLMessage reply = message.createReply();
		reply.setStringContent(String.valueOf(offer));
		reply.setPerformative(ACLPerformative.PROPOSE);
		return reply;
	}

	@Override
	protected MessageTemplate createCFPTemplate() {
        return MessageTemplate.and(
            super.createCFPTemplate(),
            MessageTemplate.ontology(parameterMessageType));
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ResourceProvisionAction(this, mapDict);
	}

	@Override
	protected void handleReject(ACLMessage message) {
		resourceProperty.add(offer);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField(new ValueAdaptor<String>(
                "Ontology",
                String.class,
                parameterMessageType) {
			@Override protected void writeThrough(String arg0) {
				parameterMessageType = arg0;
			}
		});
		e.addField(new ValueSelectionAdaptor<ResourceProperty>(
                "ResourceProperty",
                ResourceProperty.class,
                resourceProperty,
                componentOwner.getProperties(ResourceProperty.class)) {
			@Override protected void writeThrough(ResourceProperty arg0) {
				resourceProperty = arg0;
			}
		});
	}
}
