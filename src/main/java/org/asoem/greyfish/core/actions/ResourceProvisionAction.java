package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

@ClassGroup(tags="actions")
public class ResourceProvisionAction extends ContractNetResponderAction {

    @Element(name="resource")
    private ResourceProperty resourceProperty;

    @Element(name="messageType", required=false)
    private String parameterMessageType;

    private double offer;

    public ResourceProvisionAction() {
        this(new Builder());
    }

    @Override
    protected String getOntology() {
        return parameterMessageType;
    }

    @Override
    protected ACLMessage handleAccept(ACLMessage message) {
        resourceProperty.subtract(offer);

        ACLMessage reply = message.createReply();
        reply.setPerformative(ACLPerformative.INFORM);
        return reply;
    }

    @Override
    protected ACLMessage handleCFP(ACLMessage message) throws NotUnderstoodException {
        double amountRequested = 0;
        try {
            amountRequested = (Double) message.getReferenceContent();
        } catch (Exception e) {
            throw new NotUnderstoodException("Double to expected, received " + message.getReferenceContent());
        }
        offer = Math.min(amountRequested, resourceProperty.getValue());

        final ACLMessage reply = message.createReply();
        reply.setReferenceContent(offer);
        reply.setPerformative(ACLPerformative.PROPOSE);
        return reply;
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
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

    protected ResourceProvisionAction(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetResponderAction.AbstractBuilder<T> {
        private ResourceProperty resourceProperty;
        private String parameterMessageType;

        public T resourceProperty(ResourceProperty resourceProperty) { this.resourceProperty = resourceProperty; return self(); }
        public T parameterMessageType(String parameterMessageType) { this.parameterMessageType = parameterMessageType; return self(); }

        protected T fromClone(ResourceProvisionAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).
                    resourceProperty(deepClone(action.resourceProperty, mapDict)).
                    parameterMessageType(action.parameterMessageType);
            return self();
        }

        public ResourceProvisionAction build() { return new ResourceProvisionAction(this); }
    }
}
