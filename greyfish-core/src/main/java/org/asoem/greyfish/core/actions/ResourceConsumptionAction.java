package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static org.asoem.greyfish.utils.base.Callbacks.call;

@Tagged("actions")
public class ResourceConsumptionAction<A extends SpatialAgent<A, ?, ?>> extends ContractNetInitiatorAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceConsumptionAction.class);

    private String ontology;

    private Callback<? super ResourceConsumptionAction<A>, Double> interactionRadius;

    protected Callback<? super ResourceConsumptionAction<A>, Double> requestAmount;

    protected Callback<? super ResourceConsumptionAction<A>, Void> uptakeUtilization;

    private Iterable<A> sensedMates = ImmutableList.of();

    private Callback<? super ResourceConsumptionAction<A>, ?> classification;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ResourceConsumptionAction() {
        this(new Builder<A>());
    }

    @Override
    protected ImmutableACLMessage.Builder<A> createCFP(final AgentContext<A> context) {
        final A receiver = Iterables.get(sensedMates, RandomGenerators.rng().nextInt(Iterables.size(sensedMates)));
        sensedMates = ImmutableList.of();
        return ImmutableACLMessage.<A>builder()
                .sender(context.agent())
                .performative(ACLPerformative.CFP)
                .ontology(getOntology())
                        // Choose only one receiver. Adding evaluates possible candidates as receivers will decrease the performance in high density populations!
                .addReceiver(receiver)
                .content(new ResourceRequestMessage(call(requestAmount, this), call(classification, this)));
    }

    @Override
    protected ImmutableACLMessage.Builder<A> handlePropose(final ACLMessage<A> message, final AgentContext<A> context) {

        final Object messageContent = message.getContent();
        if (!(messageContent instanceof Double)) {
            throw new NotUnderstoodException("Expected payload of type Double");
        }

        final Double offer = (Double) messageContent;

        assert offer != 0 : this + ": Got (double) offer = 0. Should be refused on the provider side";

        return ImmutableACLMessage.createReply(message, context.agent())
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .content(offer);
    }

    @Override
    protected void handleInform(final ACLMessage<A> message, final AgentContext<A> context) {
        final Object messageContent = message.getContent();
        if (!(messageContent instanceof Double)) {
            throw new NotUnderstoodException("Expected a payload of type Double");
        }

        final Double offer = (Double) messageContent;
        LOGGER.info("{}: Consuming {} {}", context.agent(), offer, ontology);
        uptakeUtilization.apply(this, ImmutableMap.of("offer", offer));
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    protected boolean canInitiate(final AgentContext<A> context) {
        sensedMates = context.agent().findNeighbours(call(interactionRadius, this));
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

    protected ResourceConsumptionAction(final AbstractBuilder<A, ? extends ResourceConsumptionAction<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.requestAmount = builder.requestAmount;
        this.interactionRadius = builder.interactionRadius;
        this.uptakeUtilization = builder.uptakeUtilization;
        this.classification = builder.classification;
    }

    public static <A extends SpatialAgent<A, ?, ?>> Builder<A> with() {
        return new Builder();
    }

    public Callback<? super ResourceConsumptionAction<A>, Double> getInteractionRadius() {
        return interactionRadius;
    }

    public Callback<? super ResourceConsumptionAction<A>, Double> getRequestAmount() {
        return requestAmount;
    }

    public Callback<? super ResourceConsumptionAction<A>, Void> getUptakeUtilization() {
        return uptakeUtilization;
    }

    public static final class Builder<A extends SpatialAgent<A, ?, ?>> extends AbstractBuilder<A, ResourceConsumptionAction<A>, Builder<A>> {
        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected ResourceConsumptionAction<A> checkedBuild() {
            return new ResourceConsumptionAction<A>(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends SpatialAgent<A, ?, ?>, C extends ResourceConsumptionAction<A>, B extends AbstractBuilder<A, C, B>> extends ContractNetInitiatorAction.AbstractBuilder<A, C, B> {

        private String ontology = "food";
        private Callback<? super ResourceConsumptionAction<A>, Double> requestAmount = Callbacks.constant(1.0);
        private Callback<? super ResourceConsumptionAction<A>, Double> interactionRadius = Callbacks.constant(1.0);
        private Callback<? super ResourceConsumptionAction<A>, Void> uptakeUtilization = Callbacks.emptyCallback();
        private Callback<? super ResourceConsumptionAction<A>, ?> classification = Callbacks.constant(0.42);

        public B ontology(final String parameterMessageType) {
            this.ontology = checkNotNull(parameterMessageType);
            return self();
        }

        public B requestAmount(final Callback<? super ResourceConsumptionAction<A>, Double> amountPerRequest) {
            this.requestAmount = amountPerRequest;
            return self();
        }

        public B interactionRadius(final Callback<? super ResourceConsumptionAction<A>, Double> sensorRange) {
            this.interactionRadius = sensorRange;
            return self();
        }

        public B uptakeUtilization(final Callback<? super ResourceConsumptionAction<A>, Void> uptakeUtilization) {
            this.uptakeUtilization = checkNotNull(uptakeUtilization);
            return self();
        }

        public B classification(final Callback<? super ResourceConsumptionAction<A>, Object> classification) {
            this.classification = checkNotNull(classification);
            return self();
        }

        @Override
        protected void checkBuilder() {
            super.checkBuilder();
        }
    }
}
