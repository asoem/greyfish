package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
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
import static org.asoem.greyfish.core.eval.GreyfishExpressionFactory.compileExpression;

@ClassGroup(tags="actions")
public class ResourceConsumptionAction extends ContractNetInitiatorAction {

    @Element(name="property")
    private DoubleProperty consumerProperty = null;

    @Element(name="resourceTransformationFunction", required = false)
    private String transformationFunction = "#{x0}";

    private GreyfishExpression<ResourceConsumptionAction> transformationExpression =
            compileExpression("#{x0}").forContext(ResourceConsumptionAction.class);

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
                .source(getAgent().getId())
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
                .createReplyFrom(getAgent().getId())
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .objectContent(offer);
    }

    @Override
    protected void handleInform(ACLMessage message) throws NotUnderstoodException {
        try {
            final double offer = message.getReferenceContent(Double.class);
            consumerProperty.add(transformationExpression.evaluateAsDouble(this, offer));

            LoggerFactory.getLogger(ResourceConsumptionAction.class).debug("Added {} to {}", offer, consumerProperty);
        }
        catch (Exception e) {
            throw new NotUnderstoodException(e);
        }
    }

    @Override
    protected String getOntology() {
        return parameterMessageType;
    }

    @Override
    protected boolean canInitiate(Simulation simulation) {
        sensedMates = filter(agent.get().findNeighbours(sensorRange), Agent.class);
        sensedMates = filter(sensedMates, not(equalTo(agent.get())));
        return ! isEmpty(sensedMates);
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        checkValidity();
    }

    private void checkValidity() {
        checkNotNull(consumerProperty);
        checkNotNull(parameterMessageType);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new ValueAdaptor<String>("Ontology", String.class) {
            @Override
            protected void set(String arg0) {
                parameterMessageType = checkNotNull(arg0);
            }

            @Override
            public String get() {
                return parameterMessageType;
            }
        });
        e.add(new ValueAdaptor<Double>("Requested Amount", Double.class) {
            @Override
            protected void set(Double arg0) {
                amountPerRequest = checkNotNull(arg0);
            }

            @Override
            public Double get() {
                return amountPerRequest;
            }
        });
        e.add(new FiniteSetValueAdaptor<DoubleProperty>("Resource Storage", DoubleProperty.class) {
            @Override
            protected void set(DoubleProperty arg0) {
                consumerProperty = checkNotNull(arg0);
            }

            @Override
            public DoubleProperty get() {
                return consumerProperty;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(agent.get().getProperties(), DoubleProperty.class);
            }
        });
        e.add(ValueAdaptor.forField("Resource Transformation Function: f(#{1})", String.class, this, "transformationFunction"));
        e.add(new ValueAdaptor<Double>("Sensor Range", Double.class) {
            @Override
            protected void set(Double arg0) {
                sensorRange = checkNotNull(arg0);
            }

            @Override
            public Double get() {
                return sensorRange;
            }
        });
    }

    @Override
    public ResourceConsumptionAction deepClone(DeepCloner cloner) {
        return new ResourceConsumptionAction(this, cloner);
    }

    protected ResourceConsumptionAction(ResourceConsumptionAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.consumerProperty = cloner.continueWith(cloneable.consumerProperty, DoubleProperty.class);
        this.parameterMessageType = cloneable.parameterMessageType;
        this.sensorRange = cloneable.sensorRange;
        this.amountPerRequest = cloneable.amountPerRequest;
        this.transformationFunction = cloneable.transformationFunction;
    }

    protected ResourceConsumptionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.consumerProperty = builder.consumerProperty;
        this.parameterMessageType = builder.parameterMessageType;
        this.amountPerRequest = builder.amountPerRequest;
        this.sensorRange = builder.sensorRange;
        this.transformationFunction = builder.transformationFunction;
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ResourceConsumptionAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ResourceConsumptionAction build() { return new ResourceConsumptionAction(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetParticipantAction.AbstractBuilder<T> {
        private DoubleProperty consumerProperty = null;
        private String parameterMessageType = "";
        private double amountPerRequest = 0;
        private double sensorRange = 0;
        public String transformationFunction = "#{x0}";

        public T storesEnergyIn(DoubleProperty consumerProperty) { this.consumerProperty = checkNotNull(consumerProperty); return self(); }
        public T viaMessagesOfType(String parameterMessageType) { this.parameterMessageType = checkNotNull(parameterMessageType); return self(); }
        public T requesting(double amountPerRequest) { this.amountPerRequest = amountPerRequest; return self(); }
        public T inRange(double sensorRange) { this.sensorRange = sensorRange; return self(); }
        public T transformationFunction(String transformationFunction) { this.transformationFunction = checkNotNull(transformationFunction); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
        }
    }
}
