/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Element;

import java.util.Map;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="actions")
public class MatingReceiverAction extends ContractNetInitiatiorAction {

    @Element(name="property")
    private EvaluatedGenomeStorage collectorProperty;

    @Element(name="messageType", required=false)
    private String parameterMessageType;

    @Element(name="sensorRange", required=false)
    private double sensorRange;

    private Iterable<Individual> sensedMates;

    private MatingReceiverAction() {
        this(new Builder());
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField(new ValueSelectionAdaptor<EvaluatedGenomeStorage>("Genome Storage", EvaluatedGenomeStorage.class, collectorProperty, getComponentOwner().getProperties(EvaluatedGenomeStorage.class)) {
            @Override
            protected void writeThrough(EvaluatedGenomeStorage arg0) {
                collectorProperty = arg0;
            }
        });
        e.addField(new ValueAdaptor<String>("Message Type", String.class, parameterMessageType) {

            @Override
            protected void writeThrough(String arg0) {
                parameterMessageType = arg0;
            }
        });
        e.addField(new ValueAdaptor<Double>("Sensor Range", Double.class, sensorRange) {

            @Override
            protected void writeThrough(Double arg0) {
                sensorRange = arg0;
            }
        });
    }

    public boolean receiveGenome(Genome genome) {
        return receiveGenome(new EvaluatedGenome(genome, evaluate(genome)));
    }

    private Integer evaluate(Genome genome) {
        // TODO: implement
        return 0;
    }

    public boolean receiveGenome(Genome genome, double d) {
        return receiveGenome(new EvaluatedGenome(genome, d));
    }

    public boolean receiveGenome(EvaluatedGenome genome) {
        collectorProperty.addGenome(genome, genome.getFitness());
        if (GreyfishLogger.isTraceEnabled())
            GreyfishLogger.trace(componentOwner + " received sperm: " + genome);
        return true;
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        checkValidity();
    }

    private void checkValidity() {
        Preconditions.checkNotNull(collectorProperty);
        Preconditions.checkNotNull(parameterMessageType);
    }

    @Override
    protected ACLMessage createCFP() {
        assert(!Iterables.isEmpty(sensedMates)); // see #evaluate(Simulation)

        ACLMessage message = ACLMessage.newInstance();
        message.setPerformative(ACLPerformative.CFP);

        /*
           *
           * Choose only one. Adding all possible candidates as receivers will decrease the performance in high density populations!
           */
        message.addReceiver(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))));
        message.setOntology(parameterMessageType);

        return message;
    }

    @Override
    protected ACLMessage handlePropose(ACLMessage message) throws NotUnderstoodException {
        ACLMessage replyMessage = message.createReply();
        try {
            EvaluatedGenome evaluatedGenome = (EvaluatedGenome) message.getReferenceContent();
            receiveGenome(evaluatedGenome);
            replyMessage.setPerformative(ACLPerformative.ACCEPT_PROPOSAL);
        } catch (IllegalArgumentException e) {
            throw new NotUnderstoodException("MessageContent is not a genome");
        }

        return replyMessage;
    }

    @Override
    protected String getOntology() {
        return parameterMessageType;
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        if ( super.evaluate(simulation) ) {
            final Iterable neighbours = simulation.getSpace().findNeighbours(componentOwner.getAnchorPoint(), sensorRange);
            sensedMates = Iterables.filter(neighbours, Individual.class);

            sensedMates = Iterables.filter(sensedMates, Predicates.not(Predicates.equalTo(componentOwner)));
            return ! Iterables.isEmpty(sensedMates);
        }
        return false;
    }

    protected MatingReceiverAction(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() {  return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetResponderAction.AbstractBuilder<T> {
        private EvaluatedGenomeStorage collectorProperty;
        private String parameterMessageType;
        private double sensorRange;

        public T collectorProperty(EvaluatedGenomeStorage collectorProperty) { this.collectorProperty = collectorProperty; return self(); }
        public T parameterMessageType(String parameterMessageType) { this.parameterMessageType = parameterMessageType; return self(); }
        public T sensorRange(double sensorRange) { this.sensorRange = sensorRange; return self(); }

        protected T fromClone(MatingReceiverAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).
                    collectorProperty(deepClone(action.collectorProperty, mapDict)).
                    parameterMessageType(action.parameterMessageType).
                    sensorRange(action.sensorRange);
            return self();
        }

        public MatingReceiverAction build() { return new MatingReceiverAction(this); }
    }
}
