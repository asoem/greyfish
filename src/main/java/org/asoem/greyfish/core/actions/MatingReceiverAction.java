/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="actions")
public class MatingReceiverAction extends ContractNetInitiatiorAction {

    @Element(name="property")
    private EvaluatedGenomeStorage spermBuffer;

    @Element(name="messageType", required=false)
    private String ontology;

    @Element(name="closerThan", required=false)
    private double sensorRange;

    private Iterable<Individual> sensedMates;

    private MatingReceiverAction() {
        this(new Builder());
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField(new ValueSelectionAdaptor<EvaluatedGenomeStorage>("Genome Storage", EvaluatedGenomeStorage.class, spermBuffer, getComponentOwner().getProperties(EvaluatedGenomeStorage.class)) {
            @Override
            protected void writeThrough(EvaluatedGenomeStorage arg0) {
                spermBuffer = checkFrozen(checkNotNull(arg0));
            }
        });
        e.addField(new ValueAdaptor<String>("Message Type", String.class, ontology) {

            @Override
            protected void writeThrough(String arg0) {
                ontology = checkFrozen(checkNotNull(arg0));
            }
        });
        e.addField(new ValueAdaptor<Double>("Sensor Range", Double.class, sensorRange) {

            @Override
            protected void writeThrough(Double arg0) {
                sensorRange = checkFrozen(checkNotNull(arg0));
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
        spermBuffer.addGenome(genome, genome.getFitness());
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
    public void checkIfFreezable(Iterable<? extends GFComponent> components) {
        super.checkIfFreezable(components);
        checkNotNull(spermBuffer);
        checkNotNull(ontology);
    }

    @Override
    protected ACLMessage createCFP() {
        assert(!Iterables.isEmpty(sensedMates)); // see #evaluate(Simulation)

        ACLMessage message = ACLMessage.newInstance();
        message.setPerformative(ACLPerformative.CFP);

        // Choose only one. Adding all possible candidates as receivers will decrease the performance in high density populations!
        message.addReceiver(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))));
        message.setOntology(ontology);

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
        return ontology;
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
        this.spermBuffer = builder.spermBuffer;
        this.ontology = builder.ontology;
        this.sensorRange = builder.sensorRange;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<MatingReceiverAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public MatingReceiverAction build() { return new MatingReceiverAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetResponderAction.AbstractBuilder<T> {
        private EvaluatedGenomeStorage spermBuffer;
        private String ontology;
        private double sensorRange;

        public T storesSpermIn(EvaluatedGenomeStorage spermBuffer) { this.spermBuffer = spermBuffer; return self(); }
        public T fromMatesOfType(String ontology) { this.ontology = ontology; return self(); }
        public T closerThan(double sensorRange) { this.sensorRange = sensorRange; return self(); }

        protected T fromClone(MatingReceiverAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).
                    storesSpermIn(deepClone(action.spermBuffer, mapDict)).
                    fromMatesOfType(action.ontology).
                    closerThan(action.sensorRange);
            return self();
        }
    }
}
