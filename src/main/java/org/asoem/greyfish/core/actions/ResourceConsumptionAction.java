package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;

@ClassGroup(tags="actions")
public class ResourceConsumptionAction extends ContractNetInitiatorAction {

    @Element(name="messageType", required=false)
    private String ontology;

    @Element(name="interactionRadius")
    protected GreyfishExpression interactionRadius;

    @Element(name="requestAmount", required=false)
    protected GreyfishExpression requestAmount;

    @Element(name="resourceTransformationFunction", required = false)
    protected GreyfishExpression utilizeUptake;

    private Iterable<Agent> sensedMates;

    @SimpleXMLConstructor
    private ResourceConsumptionAction() {
        this(new Builder());
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> createCFP() {
        return ImmutableACLMessage.<Agent>with()
                .sender(agent())
                .performative(ACLPerformative.CFP)
                .ontology(getOntology())
                        // Choose only one receiver. Adding all possible candidates as receivers will decrease the performance in high density populations!
                .addReceiver(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))))
                .content(requestAmount.evaluateForContext(this).asDouble(), Double.class);
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handlePropose(ACLMessage<Agent> message) throws NotUnderstoodException {

        final double offer = message.getContent(Double.class);

        assert offer != 0 : this + ": Got (double) offer = 0. Should be refused on the provider side";

        return ImmutableACLMessage.createReply(message, agent())
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .content(offer, Double.class);
    }

    @Override
    protected void handleInform(ACLMessage<Agent> message) {
        final double offer = message.getContent(Double.class);
        utilizeUptake.evaluateForContext(this, "offer", offer);
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
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
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
        e.add("Uptake Utilization", TypedValueModels.forField("utilizeUptake", this, GreyfishExpression.class));
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
        this.utilizeUptake = cloneable.utilizeUptake;
    }

    protected ResourceConsumptionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.ontology = builder.ontology;
        this.requestAmount = builder.requestAmount;
        this.interactionRadius = builder.interactionRadius;
        this.utilizeUptake = builder.utilizeUptake;
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<ResourceConsumptionAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public ResourceConsumptionAction checkedBuild() {
            return new ResourceConsumptionAction(this);
        }
    }

    protected static abstract class AbstractBuilder<E extends ResourceConsumptionAction, T extends AbstractBuilder<E, T>> extends ContractNetParticipantAction.AbstractBuilder<E, T> {

        private String ontology = "food";
        private GreyfishExpression requestAmount = GreyfishExpressionFactory.compile("1.0");
        private GreyfishExpression interactionRadius = GreyfishExpressionFactory.compile("1.0");
        private GreyfishExpression utilizeUptake = GreyfishExpressionFactory.compile("$('this.agent.properties[\"myEnergy\"]').add(offer)");

        public T ontology(String parameterMessageType) { this.ontology = checkNotNull(parameterMessageType); return self(); }
        public T requestAmount(GreyfishExpression amountPerRequest) { this.requestAmount = amountPerRequest; return self(); }
        public T interactionRadius(GreyfishExpression sensorRange) { this.interactionRadius = sensorRange; return self(); }
        public T utilizeUptake(GreyfishExpression transformationFunction) { this.utilizeUptake = checkNotNull(transformationFunction); return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
        }
    }
}
