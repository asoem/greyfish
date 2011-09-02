package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@ClassGroup(tags="actions")
public class ResourceProvisionAction extends ContractNetParticipantAction {

    @Element(name="resource")
    private ResourceProperty resourceProperty;

    @Element(name="messageType", required=false)
    private String parameterMessageType;

    @SimpleXMLConstructor
    public ResourceProvisionAction() {
        this(new Builder());
    }

    @Override
    protected String getOntology() {
        return parameterMessageType;
    }

    @Override
    protected ACLMessage.Builder handleCFP(ACLMessage message) throws NotUnderstoodException {
        double requested;
        try {
            requested = message.getReferenceContent(Double.class);
        } catch (IllegalArgumentException e) {
            throw new NotUnderstoodException("Double content expected, received " + message);
        }

        ACLMessage.Builder ret = message.createReplyFrom(getComponentOwner().getId());

        double offer = Math.min(requested, resourceProperty.get());

        if (offer > 0) {
            ret.performative(ACLPerformative.PROPOSE).objectContent(offer);
            LoggerFactory.getLogger(ResourceProvisionAction.class).debug("Offering {}", offer);
        }
        else {
            ret.performative(ACLPerformative.REFUSE).stringContent("Nothing to offer");
            LoggerFactory.getLogger(ResourceProvisionAction.class).debug("Nothing to offer");
        }

        return ret;
    }

    @Override
    protected ACLMessage.Builder handleAccept(ACLMessage message) throws NotUnderstoodException {
        try {
            double offer = message.getReferenceContent(Double.class);
            assert resourceProperty.get() >= offer : "Values have changed unexpectedly";

            LoggerFactory.getLogger(ResourceProvisionAction.class).debug("Subtracting {}", offer);

            resourceProperty.subtract(offer);
            return message
                    .createReplyFrom(getComponentOwner().getId())
                    .performative(ACLPerformative.INFORM)
                    .objectContent(offer);
        } catch (IllegalArgumentException e) {
            throw new NotUnderstoodException("Double content expected, received " + message);
        }
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.add(new ValueAdaptor<String>(
                "Ontology",
                String.class
        ) {
            @Override
            protected void set(String arg0) {
                parameterMessageType = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public String get() {
                return parameterMessageType;
            }
        });
        e.add(new FiniteSetValueAdaptor<ResourceProperty>("ResourceProperty", ResourceProperty.class) {
            @Override
            protected void set(ResourceProperty arg0) {
                resourceProperty = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public ResourceProperty get() {
                return resourceProperty;
            }

            @Override
            public Iterable<ResourceProperty> values() {
                return Iterables.filter(getComponentOwner().getProperties(), ResourceProperty.class);
            }
        });
    }

    @Override
    public ResourceProvisionAction deepCloneHelper(CloneMap cloneMap) {
        return new ResourceProvisionAction(this, cloneMap);
    }

    protected ResourceProvisionAction(ResourceProvisionAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        this.resourceProperty = cloneMap.clone(cloneable.resourceProperty, ResourceProperty.class);
        this.parameterMessageType = cloneable.parameterMessageType;
    }

    protected ResourceProvisionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.parameterMessageType = builder.parameterMessageType;
        this.resourceProperty = builder.resourceProperty;
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        checkState(resourceProperty != null);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ResourceProvisionAction> {
        @Override protected Builder self() { return this; }
        @Override public ResourceProvisionAction build() { return new ResourceProvisionAction(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetParticipantAction.AbstractBuilder<T> {
        protected ResourceProperty resourceProperty;
        protected String parameterMessageType;

        public T resourceProperty(ResourceProperty resourceProperty) { this.resourceProperty = checkNotNull(resourceProperty); return self(); }
        public T parameterMessageType(String parameterMessageType) { this.parameterMessageType = checkNotNull(parameterMessageType); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(this.resourceProperty != null, "The ResourceProperty is mandatory");
            checkState(this.parameterMessageType != null, "The messageType is mandatory");
            super.checkBuilder();
        }
    }
}
