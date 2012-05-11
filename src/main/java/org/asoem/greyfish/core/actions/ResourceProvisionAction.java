package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@ClassGroup(tags="actions")
public class ResourceProvisionAction extends ContractNetParticipantAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceProvisionAction.class);
    @Element(name="ontology", required=false)
    private String ontology;

    private GreyfishExpression provides;

    private double providedAmount;

    private boolean proposalSent;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ResourceProvisionAction() {
        this(new Builder());
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
    protected ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message, Simulation simulation) {
        final ImmutableACLMessage.Builder<Agent> reply = ImmutableACLMessage.createReply(message, getAgent());
        if (proposalSent)
            return reply.performative(ACLPerformative.REFUSE);

        final ResourceRequestMessage requestMessage = message.getContent(ResourceRequestMessage.class);
        final double requestAmount = requestMessage.getRequestAmount();
        final double providedAmount = provides.evaluateForContext(this, ImmutableMap.of("classifier", requestMessage.getRequestClassifier())).asDouble();
        final double offeredAmount = Math.min(requestAmount, providedAmount);

        if (offeredAmount > 0) {
            reply.performative(ACLPerformative.PROPOSE).content(offeredAmount, Double.class);
            proposalSent = true;
            LOGGER.trace("{}: Offering {}", agent(), offeredAmount);
        }
        else {
            reply.performative(ACLPerformative.REFUSE).content("Nothing to offeredAmount", String.class);
            LOGGER.trace("{}: Nothing to offeredAmount", agent());
        }

        return reply;
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handleAccept(ACLMessage<Agent> message, Simulation simulation) {
            double offer = message.getContent(Double.class);

            LOGGER.info("{}: Provided {}", agent(), offer);

            this.providedAmount += offer;

            return ImmutableACLMessage.createReply(message, getAgent())
                    .performative(ACLPerformative.INFORM)
                    .content(offer, Double.class);
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
    }

    @Override
    public ResourceProvisionAction deepClone(DeepCloner cloner) {
        return new ResourceProvisionAction(this, cloner);
    }

    protected ResourceProvisionAction(ResourceProvisionAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.provides = cloneable.provides;
        this.ontology = cloneable.ontology;
    }

    protected ResourceProvisionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.provides = builder.provides;
    }

    public static Builder with() { return new Builder(); }

    public double getProvidedAmount() {
        return providedAmount;
    }

    @Override
    public void initialize() {
        super.initialize();
        providedAmount = 0;
    }

    public static final class Builder extends AbstractBuilder<ResourceProvisionAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override protected ResourceProvisionAction checkedBuild() { return new ResourceProvisionAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends ResourceProvisionAction, T extends AbstractBuilder<E,T>> extends ContractNetParticipantAction.AbstractBuilder<E,T> {
        private String ontology;
        private GreyfishExpression provides;

        public T ontology(String ontology) { this.ontology = checkNotNull(ontology); return self(); }
        public T provides(GreyfishExpression expression) {
            this.provides = checkNotNull(expression);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(ontology != null, "The messageType is mandatory");
            checkState(provides != null, "You must define ");
            super.checkBuilder();
        }


    }
}
