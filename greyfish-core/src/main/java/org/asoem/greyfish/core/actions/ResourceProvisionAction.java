package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Tagged("actions")
public class ResourceProvisionAction<A extends Agent<A, ?>> extends ContractNetParticipantAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceProvisionAction.class);

    private String ontology;

    private Callback<? super ResourceProvisionAction<A>, Double> provides;

    private double providedAmount;

    private boolean proposalSent;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ResourceProvisionAction() {
        this(new Builder<A>());
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    protected void prepareForCommunication() {
        proposalSent = false;
    }

    @Override
    protected ImmutableACLMessage.Builder<A> handleCFP(final ACLMessage<A> message) {
        final ImmutableACLMessage.Builder<A> reply = ImmutableACLMessage.createReply(message, getAgent());
        if (proposalSent)
            return reply.performative(ACLPerformative.REFUSE);

        final Object messageContent = message.getContent();
        if (! (messageContent instanceof ResourceRequestMessage))
            throw new NotUnderstoodException("Expected payload of type ResourceRequestMessage");

        final ResourceRequestMessage requestMessage = (ResourceRequestMessage) messageContent;
        final double requestAmount = requestMessage.getRequestAmount();
        final double providedAmount = provides.apply(this, ImmutableMap.of("classifier", requestMessage.getRequestClassifier()));
        final double offeredAmount = Math.min(requestAmount, providedAmount);

        if (offeredAmount > 0) {
            reply.performative(ACLPerformative.PROPOSE).content(offeredAmount, Double.class);
            proposalSent = true;
            LOGGER.trace("{}: Offering {}", agent(), offeredAmount);
        } else {
            reply.performative(ACLPerformative.REFUSE).content("Nothing to offeredAmount", String.class);
            LOGGER.trace("{}: Nothing to offeredAmount", agent());
        }

        return reply;
    }

    @Override
    protected ImmutableACLMessage.Builder<A> handleAccept(final ACLMessage<A> message) {
        final Object messageContent = message.getContent();
        if (! (messageContent instanceof Double))
            throw new NotUnderstoodException("Expected payload of type Double");

        final Double offer = (Double) messageContent;

        LOGGER.info("{}: Provided {}", agent(), offer);

        this.providedAmount += offer;

        return ImmutableACLMessage.createReply(message, getAgent())
                .performative(ACLPerformative.INFORM)
                .content(offer, Double.class);
    }

    @Override
    public ResourceProvisionAction<A> deepClone(final DeepCloner cloner) {
        return new ResourceProvisionAction<A>(this, cloner);
    }

    protected ResourceProvisionAction(final ResourceProvisionAction<A> cloneable, final DeepCloner cloner) {
        super(cloneable, cloner);
        this.provides = cloneable.provides;
        this.ontology = cloneable.ontology;
    }

    protected ResourceProvisionAction(final AbstractBuilder<A, ? extends ResourceProvisionAction<A>, ? extends AbstractBuilder<A,?,?>> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.provides = builder.provides;
    }

    public static <A extends Agent<A, ?>> Builder<A> with() {
        return new Builder();
    }

    public double getProvidedAmount() {
        return providedAmount;
    }

    @Override
    public void initialize() {
        super.initialize();
        providedAmount = 0;
    }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, ResourceProvisionAction<A>, Builder<A>> {
        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected ResourceProvisionAction<A> checkedBuild() {
            return new ResourceProvisionAction<A>(this);
        }
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends ResourceProvisionAction<A>, B extends AbstractBuilder<A, C, B>> extends ContractNetParticipantAction.AbstractBuilder<A, C, B> {
        private String ontology;
        private Callback<? super ResourceProvisionAction<A>, Double> provides;

        public B ontology(final String ontology) {
            this.ontology = checkNotNull(ontology);
            return self();
        }

        public B provides(final Callback<? super ResourceProvisionAction<A>, Double> expression) {
            this.provides = checkNotNull(expression);
            return self();
        }

        @Override
        protected void checkBuilder() {
            checkState(ontology != null, "The messageType is mandatory");
            checkState(provides != null, "You must define what this resource should provide");
            super.checkBuilder();
        }


    }
}