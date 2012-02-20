package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@ClassGroup(tags="actions")
public class ResourceProvisionAction extends ContractNetParticipantAction {

    @Element(name="resource")
    private DoubleProperty resourceProperty;

    @Element(name="ontology", required=false)
    private String ontology;

    @SimpleXMLConstructor
    public ResourceProvisionAction() {
        this(new Builder());
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message, Simulation simulation) throws NotUnderstoodException {
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
    protected ImmutableACLMessage.Builder<Agent> handleAccept(ACLMessage<Agent> message, Simulation simulation) throws NotUnderstoodException {
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
                ontology = checkNotNull(arg0);
            }

            @Override
            public String get() {
                return ontology;
            }
        });
        e.add("ResourceProperty", new SetAdaptor<DoubleProperty>(DoubleProperty.class) {
            @Override
            protected void set(DoubleProperty arg0) {
                resourceProperty = checkNotNull(arg0);
            }

            @Override
            public DoubleProperty get() {
                return resourceProperty;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(agent().getProperties(), DoubleProperty.class);
            }
        });
    }

    @Override
    public ResourceProvisionAction deepClone(DeepCloner cloner) {
        return new ResourceProvisionAction(this, cloner);
    }

    protected ResourceProvisionAction(ResourceProvisionAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.resourceProperty = cloner.cloneField(cloneable.resourceProperty, DoubleProperty.class);
        this.ontology = cloneable.ontology;
    }

    protected ResourceProvisionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.resourceProperty = builder.resourceProperty;
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<ResourceProvisionAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public ResourceProvisionAction checkedBuild() { return new ResourceProvisionAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends ResourceProvisionAction, T extends AbstractBuilder<E,T>> extends ContractNetParticipantAction.AbstractBuilder<E,T> {
        protected DoubleProperty resourceProperty;
        protected String ontology = "food";

        public T resourceProperty(DoubleProperty resourceProperty) { this.resourceProperty = checkNotNull(resourceProperty); return self(); }
        public T ontology(String ontology) { this.ontology = checkNotNull(ontology); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(this.resourceProperty != null, "The ResourceProperty is mandatory");
            checkState(this.ontology != null, "The messageType is mandatory");
            super.checkBuilder();
        }
    }
}
