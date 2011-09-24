/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.Agent;
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
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;

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

    private Iterable<Agent> sensedMates;

    @SuppressWarnings("unused")
    private MatingReceiverAction() {
        this(new Builder());
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new FiniteSetValueAdaptor<EvaluatedGenomeStorage>("ImmutableGenome Storage", EvaluatedGenomeStorage.class) {
            @Override
            protected void set(EvaluatedGenomeStorage arg0) {
                spermBuffer = checkNotNull(arg0);
            }

            @Override
            public EvaluatedGenomeStorage get() {
                return spermBuffer;
            }

            @Override
            public Iterable<EvaluatedGenomeStorage> values() {
                return filter(getAllComponents(), EvaluatedGenomeStorage.class);
            }
        });
        e.add(new ValueAdaptor<String>("Message Type", String.class) {
            @Override
            protected void set(String arg0) {
                ontology = checkNotNull(arg0);
            }

            @Override
            public String get() {
                return ontology;
            }
        });
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

    private boolean receiveGenome(EvaluatedGenome genome) {
        spermBuffer.addGenome(genome, genome.getFitness());
        getAgent().getLog().add("spermReceived", 1);

        LOGGER.trace(getAgent() + " received sperm: " + genome);

        return true;
    }

    @Override
    public void checkConsistency() {
        super.checkConsistency();
        checkNotNull(spermBuffer);
        checkNotNull(ontology);
    }

    @Override
    protected ACLMessage.Builder createCFP() {
        assert(!Iterables.isEmpty(sensedMates)); // see #evaluateConditions(Simulation)

        return ACLMessage.with()
                .source(getAgent().getId())
                .performative(ACLPerformative.CFP)
                .ontology(ontology)
                        // Choose only one receiver. Adding all possible candidates as receivers will decrease the performance in high density populations!
                .addDestinations(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))).getId());
    }

    @Override
    protected ACLMessage.Builder handlePropose(ACLMessage message) throws NotUnderstoodException {
        ACLMessage.Builder builder = message.createReplyFrom(this.getAgent().getId());
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
    protected boolean canInitiate(Simulation simulation) {
        final Iterable neighbours = agent.findNeighbours(sensorRange);
        sensedMates = filter(neighbours, Agent.class);
        sensedMates = filter(sensedMates, not(equalTo(agent)));
        LOGGER.debug("Found {} possible mate(s)", Iterables.size(sensedMates));
        return ! Iterables.isEmpty(sensedMates);
    }

    @Override
    public MatingReceiverAction deepClone(DeepCloner cloner) {
        return new MatingReceiverAction(this, cloner);
    }

    private MatingReceiverAction(MatingReceiverAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.spermBuffer = cloner.continueWith(cloneable.spermBuffer, EvaluatedGenomeStorage.class);
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
                LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": sensorRange is <= 0 '" + sensorRange + "'");
            if (Strings.isNullOrEmpty(ontology))
                LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": ontology is invalid '" + ontology + "'");
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
