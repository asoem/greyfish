package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Element;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.isEmpty;

@ClassGroup(tags="actions")
public class ResourceConsumptionAction extends ContractNetInitiatiorAction {

    @Element(name="property")
    private DoubleProperty consumerProperty = null;

    @Element(name="messageType", required=false)
    private String parameterMessageType = "";

    @Element(name="amountPerRequest", required=false)
    protected double amountPerRequest = 0;

    @Element(name="sensorRange")
    private double sensorRange = 0;

    private Iterable<Agent> sensedMates;

    @SimpleXMLConstructor
    private ResourceConsumptionAction() {
        this(new Builder());
    }

    @Override
    protected ACLMessage.Builder createCFP() {
        return ACLMessage.with()
                .source(getComponentOwner().getId())
                .performative(ACLPerformative.CFP)
                .ontology(getOntology())
                        // Choose only one receiver. Adding all possible candidates as receivers will decrease the performance in high density populations!
                .addDestinations(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))).getId())
                .objectContent(amountPerRequest);
    }

    @Override
    protected ACLMessage.Builder handlePropose(ACLMessage message) throws NotUnderstoodException {

        final double offer = message.getReferenceContent(Double.class);

        assert offer != 0 : this + ": Got (double) offer = 0. Should be refused on the provider side";

        return message
                .createReplyFrom(getComponentOwner().getId())
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .objectContent(offer);
    }

    @Override
    protected void handleInform(ACLMessage message) throws NotUnderstoodException {
        try {
            final double offer = message.getReferenceContent(Double.class);
            consumerProperty.add(offer);

            LoggerFactory.getLogger(ResourceConsumptionAction.class).debug("Added {} to {}", offer, consumerProperty);
        }
        catch (Exception e) {
            throw new NotUnderstoodException();
        }
    }

    @Override
    protected String getOntology() {
        return parameterMessageType;
    }

    @Override
    public boolean evaluateInternalState(Simulation simulation) {
        sensedMates = filter(simulation.findObjects(getComponentOwner(), sensorRange), Agent.class);
        sensedMates = filter(sensedMates, not(equalTo(getComponentOwner())));
        return ! isEmpty(sensedMates);
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        checkValidity();
    }

    private void checkValidity() {
        checkNotNull(consumerProperty);
        checkNotNull(parameterMessageType);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.add(new ValueAdaptor<String>("Ontology", String.class) {
            @Override
            protected void set(String arg0) {
                parameterMessageType = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public String get() {
                return parameterMessageType;
            }
        });
        e.add(new ValueAdaptor<Double>("Amount", Double.class) {
            @Override
            protected void set(Double arg0) {
                amountPerRequest = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public Double get() {
                return amountPerRequest;
            }
        });
        e.add(new FiniteSetValueAdaptor<DoubleProperty>("Destination", DoubleProperty.class) {
            @Override
            protected void set(DoubleProperty arg0) {
                consumerProperty = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public DoubleProperty get() {
                return consumerProperty;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(getComponentOwner().getProperties(), DoubleProperty.class);
            }
        });
        e.add(new ValueAdaptor<Double>("Sensor Range", Double.class) {
            @Override
            protected void set(Double arg0) {
                sensorRange = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public Double get() {
                return sensorRange;
            }
        });
    }

    @Override
    public ResourceConsumptionAction deepCloneHelper(CloneMap cloneMap) {
        return new ResourceConsumptionAction(this, cloneMap);
    }

    protected ResourceConsumptionAction(ResourceConsumptionAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        this.consumerProperty = cloneMap.clone(cloneable.consumerProperty, DoubleProperty.class);
        this.parameterMessageType = cloneable.parameterMessageType;
        this.sensorRange = cloneable.sensorRange;
        this.amountPerRequest = cloneable.amountPerRequest;
    }

    protected ResourceConsumptionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.consumerProperty = builder.consumerProperty;
        this.parameterMessageType = builder.parameterMessageType;
        this.amountPerRequest = builder.amountPerRequest;
        this.sensorRange = builder.sensorRange;
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ResourceConsumptionAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ResourceConsumptionAction build() { return new ResourceConsumptionAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetParticipantAction.AbstractBuilder<T> {
        private DoubleProperty consumerProperty = null;
        private String parameterMessageType = "";
        private double amountPerRequest = 0;
        private double sensorRange = 0;

        public T storesEnergyIn(DoubleProperty consumerProperty) { this.consumerProperty = consumerProperty; return self(); }
        public T viaMessagesOfType(String parameterMessageType) { this.parameterMessageType = parameterMessageType; return self(); }
        public T requesting(double amountPerRequest) { this.amountPerRequest = amountPerRequest; return self(); }
        public T inRange(double sensorRange) { this.sensorRange = sensorRange; return self(); }
    }
}
