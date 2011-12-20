package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
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
    protected ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message) throws NotUnderstoodException {
        double requested;
        try {
            requested = message.getContent(Double.class);
        } catch (IllegalArgumentException e) {
            throw new NotUnderstoodException("Double content expected, received " + message);
        }

        ImmutableACLMessage.Builder<Agent> ret = ImmutableACLMessage.createReply(message, getAgent());

        double offer = Math.min(requested, resourceProperty.get());

        if (offer > 0) {
            ret.performative(ACLPerformative.PROPOSE).content(offer, Double.class);
            LoggerFactory.getLogger(ResourceProvisionAction.class).debug("Offering {}", offer);
        }
        else {
            ret.performative(ACLPerformative.REFUSE).content("Nothing to offer", String.class);
            LoggerFactory.getLogger(ResourceProvisionAction.class).debug("Nothing to offer");
        }

        return ret;
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleAccept(ACLMessage<Agent> message) throws NotUnderstoodException {
        try {
            double offer = message.getContent(Double.class);
            assert resourceProperty.get() >= offer : "Values have changed unexpectedly";

            LoggerFactory.getLogger(ResourceProvisionAction.class).debug("Subtracting {}", offer);

            resourceProperty.subtract(offer);
            return ImmutableACLMessage.createReply(message, getAgent())
                    .performative(ACLPerformative.INFORM)
                    .content(offer, Double.class);
        } catch (IllegalArgumentException e) {
            throw new NotUnderstoodException("Double content expected, received " + message);
        }
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Ontology", new AbstractTypedValueModel<String>(
        ) {
            @Override
            protected void set(String arg0) {
                parameterMessageType = checkNotNull(arg0);
            }

            @Override
            public String get() {
                return parameterMessageType;
            }
        });
        e.add("ResourceProperty", new SetAdaptor<ResourceProperty>(ResourceProperty.class) {
            @Override
            protected void set(ResourceProperty arg0) {
                resourceProperty = checkNotNull(arg0);
            }

            @Override
            public ResourceProperty get() {
                return resourceProperty;
            }

            @Override
            public Iterable<ResourceProperty> values() {
                return Iterables.filter(agent().getProperties(), ResourceProperty.class);
            }
        });
    }

    @Override
    public ResourceProvisionAction deepClone(DeepCloner cloner) {
        return new ResourceProvisionAction(this, cloner);
    }

    protected ResourceProvisionAction(ResourceProvisionAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.resourceProperty = cloner.cloneField(cloneable.resourceProperty, ResourceProperty.class);
        this.parameterMessageType = cloneable.parameterMessageType;
    }

    protected ResourceProvisionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.parameterMessageType = builder.parameterMessageType;
        this.resourceProperty = builder.resourceProperty;
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<ResourceProvisionAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public ResourceProvisionAction checkedBuild() { return new ResourceProvisionAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends ResourceProvisionAction, T extends AbstractBuilder<E,T>> extends ContractNetParticipantAction.AbstractBuilder<E,T> {
        protected ResourceProperty resourceProperty;
        protected String parameterMessageType;

        public T resourceProperty(ResourceProperty resourceProperty) { this.resourceProperty = checkNotNull(resourceProperty); return self(); }
        public T classification(String parameterMessageType) { this.parameterMessageType = checkNotNull(parameterMessageType); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(this.resourceProperty != null, "The ResourceProperty is mandatory");
            checkState(this.parameterMessageType != null, "The messageType is mandatory");
            super.checkBuilder();
        }
    }
}
