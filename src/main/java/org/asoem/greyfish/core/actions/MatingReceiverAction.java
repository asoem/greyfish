/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.io.GreyfishLogger.CORE_LOGGER;
import static org.asoem.greyfish.core.io.GreyfishLogger.GFACTIONS_LOGGER;

/**
 * @author christoph
 *
 */
@ClassGroup(tags="actions")
public class MatingReceiverAction extends ContractNetInitiatorAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatingReceiverAction.class);

    @Element(name="property")
    private EvaluatedGenomeStorage spermBuffer;

    @Element(name="messageType", required=false)
    private String ontology;

    @Element(name="sensorRange", required=false)
    private double sensorRange;

    private Iterable<IndividualInterface> sensedMates;

    @SuppressWarnings("unused")
    private MatingReceiverAction() {
        this(new Builder());
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.add(new FiniteSetValueAdaptor<EvaluatedGenomeStorage>("Genome Storage", EvaluatedGenomeStorage.class) {
            @Override
            protected void set(EvaluatedGenomeStorage arg0) {
                spermBuffer = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public EvaluatedGenomeStorage get() {
                return spermBuffer;
            }

            @Override
            public Iterable<EvaluatedGenomeStorage> values() {
                return Iterables.filter(getComponentOwner().getProperties(), EvaluatedGenomeStorage.class);
            }
        });
        e.add(new ValueAdaptor<String>("Message Type", String.class) {
            @Override
            protected void set(String arg0) {
                ontology = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public String get() {
                return ontology;
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

    public boolean receiveGenome(EvaluatedGenome genome) {
        spermBuffer.addGenome(genome, genome.getFitness());
        getComponentOwner().getLog().add("spermReceived", 1);

        LOGGER.trace(getComponentOwner() + " received sperm: " + genome);

        return true;
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        checkNotNull(spermBuffer);
        checkNotNull(ontology);
    }

    @Override
    protected ACLMessage.Builder createCFP() {
        assert(!Iterables.isEmpty(sensedMates)); // see #evaluateConditions(Simulation)

        return ACLMessage.with()
                .source(getComponentOwner().getId())
                .performative(ACLPerformative.CFP)
                .ontology(ontology)
                        // Choose only one receiver. Adding all possible candidates as receivers will decrease the performance in high density populations!
                .addDestinations(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))).getId());
    }

    @Override
    protected ACLMessage.Builder handlePropose(ACLMessage message) throws NotUnderstoodException {
        ACLMessage.Builder builder = message.createReplyFrom(this.getComponentOwner().getId());
        try {
            EvaluatedGenome evaluatedGenome = message.getReferenceContent(EvaluatedGenome.class);
            receiveGenome(evaluatedGenome);
            builder.performative(ACLPerformative.ACCEPT_PROPOSAL);
        } catch (IllegalArgumentException e) {
            throw new NotUnderstoodException("MessageContent is not a genome");
        }

        return builder;
    }

    @Override
    protected String getOntology() {
        return ontology;
    }

    @Override
    public boolean evaluateInternalState(Simulation simulation) {
        final Iterable neighbours = simulation.getSpace().findNeighbours(getComponentOwner().getAnchorPoint(), sensorRange);
        sensedMates = Iterables.filter(neighbours, IndividualInterface.class);
        sensedMates = Iterables.filter(sensedMates, Predicates.not(Predicates.equalTo(getComponentOwner())));
        if (GFACTIONS_LOGGER.isDebugEnabled())
            GFACTIONS_LOGGER.debug(MatingReceiverAction.class.getSimpleName() + ": Found " + Iterables.size(sensedMates) + " possible mate(s)");
        return ! Iterables.isEmpty(sensedMates);
    }

    @Override
    public MatingReceiverAction deepCloneHelper(CloneMap cloneMap) {
        return new MatingReceiverAction(this, cloneMap);
    }

    private MatingReceiverAction(MatingReceiverAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        this.spermBuffer = cloneMap.clone(cloneable.spermBuffer, EvaluatedGenomeStorage.class);
        this.ontology = cloneable.ontology;
        this.sensorRange = cloneable.sensorRange;
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
        @Override public MatingReceiverAction build() {
            checkState(spermBuffer != null, "Builder must define a valid spermBuffer.");
            if (sensorRange <= 0)
                CORE_LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": sensorRange is <= 0 '" + sensorRange + "'");
            if (Strings.isNullOrEmpty(ontology))
                CORE_LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": ontology is invalid '" + ontology + "'");
            return new MatingReceiverAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetParticipantAction.AbstractBuilder<T> {
        protected EvaluatedGenomeStorage spermBuffer = null;
        protected String ontology = "";
        protected double sensorRange = 1.0;

        public T storesSpermIn(EvaluatedGenomeStorage spermBuffer) { this.spermBuffer = checkNotNull(spermBuffer); return self(); }
        public T fromMatesOfType(String ontology) { this.ontology = checkNotNull(ontology); return self(); }
        public T closerThan(double sensorRange) { this.sensorRange = sensorRange; return self(); }
    }
}
