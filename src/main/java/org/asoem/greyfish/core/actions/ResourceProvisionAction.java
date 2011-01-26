package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
    protected ACLMessage.Builder handleAccept(ACLMessage message) {
        resourceProperty.subtract(offer);

        return message.replyFrom(componentOwner)
                .performative(ACLPerformative.INFORM);
    }

    @Override
    protected ACLMessage.Builder handleCFP(ACLMessage message) throws NotUnderstoodException {
        double amountRequested;
        try {
            amountRequested = message.getReferenceContent(Double.class);
        } catch (IllegalArgumentException e) {
            throw new NotUnderstoodException("Double content expected, received " + message);
        }

        ACLMessage.Builder ret = message.replyFrom(componentOwner);

        offer = Math.min(amountRequested, resourceProperty.getValue());

        if (offer > 0)
            ret.performative(ACLPerformative.PROPOSE).objectContent(offer);
        else
            ret.performative(ACLPerformative.REFUSE);

        return ret;
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
                parameterMessageType = checkFrozen(checkNotNull(arg0));
            }
        });
        e.addField(new ValueSelectionAdaptor<ResourceProperty>(
                "ResourceProperty",
                ResourceProperty.class,
                resourceProperty,
                componentOwner.getProperties(ResourceProperty.class)) {
            @Override protected void writeThrough(ResourceProperty arg0) {
                resourceProperty = checkFrozen(checkNotNull(arg0));
            }
        });
    }

    @Override
    protected ResourceProvisionAction deepCloneHelper(CloneMap cloneMap) {
        return new ResourceProvisionAction(this, cloneMap);
    }

    private ResourceProvisionAction(ResourceProvisionAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        this.resourceProperty = deepClone(cloneable.resourceProperty, cloneMap);
        this.parameterMessageType = cloneable.parameterMessageType;
    }

    protected ResourceProvisionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.parameterMessageType = builder.parameterMessageType;
        this.resourceProperty = builder.resourceProperty;
    }

    @Override
    public void checkIfFreezable(Iterable<? extends GFComponent> components) {
        super.checkIfFreezable(components);
        checkState(resourceProperty != null);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ResourceProvisionAction> {
        @Override protected Builder self() {  return this; }
        public ResourceProvisionAction build() {
            checkState(this.resourceProperty != null, "The ResourceProperty is mandatory");
            checkState(this.parameterMessageType != null, "The messageType is mandatory");
            return new ResourceProvisionAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetResponderAction.AbstractBuilder<T> {
        protected ResourceProperty resourceProperty;
        protected String parameterMessageType;

        public T resourceProperty(ResourceProperty resourceProperty) { this.resourceProperty = checkNotNull(resourceProperty); return self(); }
        public T parameterMessageType(String parameterMessageType) { this.parameterMessageType = checkNotNull(parameterMessageType); return self(); }
    }
}
