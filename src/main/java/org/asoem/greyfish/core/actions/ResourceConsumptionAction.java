package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;

@ClassGroup(tags="actions")
public class ResourceConsumptionAction extends ContractNetInitiatorAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceConsumptionAction.class);
    @Element(name="ontology", required=false)
    private String ontology;

    @Element(name="interactionRadius")
    protected GreyfishExpression interactionRadius;

    @Element(name="requestAmount", required=false)
    protected GreyfishExpression requestAmount;

    @Element(name="uptakeUtilization", required = false)
    protected GreyfishExpression uptakeUtilization;

    private Iterable<Agent> sensedMates;

    private GreyfishExpression classification;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ResourceConsumptionAction() {
        this(new Builder());
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> createCFP(Simulation simulation) {
        return ImmutableACLMessage.<Agent>with()
                .sender(agent())
                .performative(ACLPerformative.CFP)
                .ontology(getOntology())
                        // Choose only one receiver. Adding evaluates possible candidates as receivers will decrease the performance in high density populations!
                .setReceivers(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))))
                .content(new ResourceRequestMessage(requestAmount.evaluateForContext(this).asDouble(), classification.evaluateForContext(this).asString()), ResourceRequestMessage.class);
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handlePropose(ACLMessage<Agent> message, Simulation simulation) throws NotUnderstoodException {

        final double offer = message.getContent(Double.class);

        assert offer != 0 : this + ": Got (double) offer = 0. Should be refused on the provider side";

        return ImmutableACLMessage.createReply(message, agent())
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .content(offer, Double.class);
    }

    @Override
    protected void handleInform(ACLMessage<Agent> message, Simulation simulation) {
        final double offer = message.getContent(Double.class);
        LOGGER.info("Consuming {} {}", offer, ontology);
        uptakeUtilization.evaluateForContext(this, "offer", offer);
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    protected boolean canInitiate(Simulation simulation) {
        sensedMates = simulation.findNeighbours(agent(), interactionRadius.evaluateForContext(this).asDouble());
        return ! isEmpty(sensedMates);
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
        e.add("Sensor Range", TypedValueModels.forField("interactionRadius", this, GreyfishExpression.class));
        e.add("Requested Amount", TypedValueModels.forField("requestAmount", this, GreyfishExpression.class));
        e.add("Uptake Utilization", TypedValueModels.forField("uptakeUtilization", this, GreyfishExpression.class));
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

    protected ResourceConsumptionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.requestAmount = builder.requestAmount;
        this.interactionRadius = builder.interactionRadius;
        this.uptakeUtilization = builder.uptakeUtilization;
        this.classification = builder.classification;
    }

    public static Builder with() { return new Builder(); }

    public GreyfishExpression getInteractionRadius() {
        return interactionRadius;
    }

    public GreyfishExpression getRequestAmount() {
        return requestAmount;
    }

    public GreyfishExpression getUptakeUtilization() {
        return uptakeUtilization;
    }

    public static final class Builder extends AbstractBuilder<ResourceConsumptionAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override protected ResourceConsumptionAction checkedBuild() {
            return new ResourceConsumptionAction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends ResourceConsumptionAction, T extends AbstractBuilder<E, T>> extends ContractNetParticipantAction.AbstractBuilder<E, T> {

        private String ontology = "food";
        private GreyfishExpression requestAmount = GreyfishExpressionFactoryHolder.compile("1.0");
        private GreyfishExpression interactionRadius = GreyfishExpressionFactoryHolder.compile("1.0");
        private GreyfishExpression uptakeUtilization = GreyfishExpressionFactoryHolder.compile("$('this.agent.properties[\"myEnergy\"]').add(offer)");
        private GreyfishExpression classification = GreyfishExpressionFactoryHolder.compile("0.42");

        public T ontology(String parameterMessageType) { this.ontology = checkNotNull(parameterMessageType); return self(); }
        public T requestAmount(GreyfishExpression amountPerRequest) { this.requestAmount = amountPerRequest; return self(); }
        public T interactionRadius(GreyfishExpression sensorRange) { this.interactionRadius = sensorRange; return self(); }
        public T uptakeUtilization(GreyfishExpression uptakeUtilization) { this.uptakeUtilization = checkNotNull(uptakeUtilization); return self(); }
        public T classification(GreyfishExpression classification) { this.classification = checkNotNull(classification); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
        }
    }
}
