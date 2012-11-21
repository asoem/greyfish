package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static org.asoem.greyfish.utils.base.Callbacks.call;

@Tagged("actions")
public class ResourceConsumptionAction<A extends Agent<A, ?, ?>> extends ContractNetInitiatorAction<A> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ResourceConsumptionAction.class);

    @Element(name = "ontology", required = false)
    private String ontology;

    @Element(name = "interactionRadius")
    private Callback<? super ResourceConsumptionAction<A>, Double> interactionRadius;

    @Element(name = "requestAmount", required = false)
    protected Callback<? super ResourceConsumptionAction<A>, Double> requestAmount;

    @Element(name = "uptakeUtilization", required = false)
    protected Callback<? super ResourceConsumptionAction<A>, Void> uptakeUtilization;

    private Iterable<A> sensedMates = ImmutableList.of();

    private Callback<? super ResourceConsumptionAction<A>, ?> classification;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ResourceConsumptionAction() {
        this(new Builder());
    }

    @Override
    protected ImmutableACLMessage.Builder<A> createCFP(Simulation<?,A,?,?> simulation) {
        final A receiver = Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates)));
        sensedMates = ImmutableList.of();
        return ImmutableACLMessage.<A>with()
                .sender(agent())
                .performative(ACLPerformative.CFP)
                .ontology(getOntology())
                        // Choose only one receiver. Adding evaluates possible candidates as receivers will decrease the performance in high density populations!
                .setReceivers(receiver)
                .content(new ResourceRequestMessage(call(requestAmount, this), call(classification, this)), ResourceRequestMessage.class);
    }

    @Override
    protected ImmutableACLMessage.Builder<A> handlePropose(ACLMessage<A> message, Simulation<?,A,?,?> simulation) throws NotUnderstoodException {

        final double offer = message.getContent(Double.class);

        assert offer != 0 : this + ": Got (double) offer = 0. Should be refused on the provider side";

        return ImmutableACLMessage.createReply(message, agent())
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .content(offer, Double.class);
    }

    @Override
    protected void handleInform(ACLMessage<A> message, Simulation<?,A,?,?> simulation) {
        final double offer = message.getContent(Double.class);
        LOGGER.info("{}: Consuming {} {}", agent(), offer, ontology);
        uptakeUtilization.apply(this, ArgumentMap.of("offer", offer));
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    protected boolean canInitiate(Simulation<?,A,?,?> simulation) {
        sensedMates = simulation.findNeighbours(agent(), call(interactionRadius, this));
        return !isEmpty(sensedMates);
    }

    @Override
    public void initialize() {
        super.initialize();
        checkValidity();
    }

    private void checkValidity() {
        checkNotNull(ontology);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Ontology", TypedValueModels.forField("ontology", this, String.class));
        e.add("Sensor Range", TypedValueModels.forField("interactionRadius", this, new TypeToken<Callback<? super ResourceConsumptionAction, Double>>() {
        }));
        e.add("Requested Amount", TypedValueModels.forField("requestAmount", this, new TypeToken<Callback<? super ResourceConsumptionAction, Double>>() {
        }));
        e.add("Uptake Utilization", TypedValueModels.forField("uptakeUtilization", this, new TypeToken<Callback<? super ResourceConsumptionAction, Void>>() {
        }));
    }

    @Override
    public ResourceConsumptionAction deepClone(DeepCloner cloner) {
        return new ResourceConsumptionAction(this, cloner);
    }

    protected ResourceConsumptionAction(ResourceConsumptionAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.ontology = cloneable.ontology;
        this.interactionRadius = cloneable.interactionRadius;
        this.requestAmount = cloneable.requestAmount;
        this.uptakeUtilization = cloneable.uptakeUtilization;
        this.classification = cloneable.classification;
    }

    protected ResourceConsumptionAction(AbstractBuilder<A, ? extends ResourceConsumptionAction<A>, ? extends AbstractBuilder<A,?,?>> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.requestAmount = builder.requestAmount;
        this.interactionRadius = builder.interactionRadius;
        this.uptakeUtilization = builder.uptakeUtilization;
        this.classification = builder.classification;
    }

    public static Builder with() {
        return new Builder();
    }

    public Callback<? super ResourceConsumptionAction, Double> getInteractionRadius() {
        return interactionRadius;
    }

    public Callback<? super ResourceConsumptionAction, Double> getRequestAmount() {
        return requestAmount;
    }

    public Callback<? super ResourceConsumptionAction, Void> getUptakeUtilization() {
        return uptakeUtilization;
    }

    public static final class Builder<A extends Agent<A, ?, ?>> extends AbstractBuilder<A, ResourceConsumptionAction<A>, Builder<A>> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected ResourceConsumptionAction checkedBuild() {
            return new ResourceConsumptionAction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, ?, ?>, C extends ResourceConsumptionAction, B extends AbstractBuilder<A, C, B>> extends ContractNetInitiatorAction.AbstractBuilder<A, C, B> {

        private String ontology = "food";
        private Callback<? super ResourceConsumptionAction, Double> requestAmount = Callbacks.constant(1.0);
        private Callback<? super ResourceConsumptionAction, Double> interactionRadius = Callbacks.constant(1.0);
        private Callback<? super ResourceConsumptionAction, Void> uptakeUtilization = Callbacks.emptyCallback();
        private Callback<? super ResourceConsumptionAction, ?> classification = Callbacks.constant(0.42);

        public B ontology(String parameterMessageType) {
            this.ontology = checkNotNull(parameterMessageType);
            return self();
        }

        public B requestAmount(Callback<? super ResourceConsumptionAction<A>, Double> amountPerRequest) {
            this.requestAmount = amountPerRequest;
            return self();
        }

        public B interactionRadius(Callback<? super ResourceConsumptionAction<A>, Double> sensorRange) {
            this.interactionRadius = sensorRange;
            return self();
        }

        public B uptakeUtilization(Callback<? super ResourceConsumptionAction<A>, Void> uptakeUtilization) {
            this.uptakeUtilization = checkNotNull(uptakeUtilization);
            return self();
        }

        public B classification(Callback<? super ResourceConsumptionAction<A>, Object> classification) {
            this.classification = checkNotNull(classification);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
        }
    }
}
