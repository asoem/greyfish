/**
 *
 */
package org.asoem.greyfish.core.actions;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
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
        e.add("ImmutableGenome Storage", new SetAdaptor<EvaluatedGenomeStorage>(EvaluatedGenomeStorage.class) {
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
                return filter(agent().getProperties(), EvaluatedGenomeStorage.class);
            }
        });
        e.add("Message Type", new AbstractTypedValueModel<String>() {
            @Override
            protected void set(String arg0) {
                ontology = checkNotNull(arg0);
            }

            @Override
            public String get() {
                return ontology;
            }
        });
        e.add("Sensor Range", new AbstractTypedValueModel<Double>() {
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

    private void receiveGenome(EvaluatedGenome genome) {
        spermBuffer.addGenome(genome, genome.getFitness());
        LOGGER.trace(getAgent() + " received sperm: " + genome);
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> createCFP() {
        int sensedMatesCount = Iterables.size(sensedMates);
        assert(sensedMatesCount > 0); // see #evaluateCondition(Simulation)

        return ImmutableACLMessage.<Agent>with()
                .sender(agent())
                .performative(ACLPerformative.CFP)
                .ontology(ontology)
                        // Choose only one receiver. Adding all possible candidates as receivers will decrease the performance in high density populations!
                .addReceiver(
                        ((sensedMatesCount) == 1
                                ? sensedMates.iterator().next()
                                : Iterables.get(sensedMates, RandomUtils.nextInt(sensedMatesCount))
                        ));
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handlePropose(ACLMessage<Agent> message) throws NotUnderstoodException {
        ImmutableACLMessage.Builder<Agent> builder = ImmutableACLMessage.createReply(message, agent());
        try {
            EvaluatedGenome evaluatedGenome = message.getContent(EvaluatedGenome.class);
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
        sensedMates = simulation.findNeighbours(agent(), sensorRange);
        LOGGER.debug("Found {} possible mate(s)", Iterables.size(sensedMates));
        return ! Iterables.isEmpty(sensedMates);
    }

    @Override
    public MatingReceiverAction deepClone(DeepCloner cloner) {
        return new MatingReceiverAction(this, cloner);
    }

    private MatingReceiverAction(MatingReceiverAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.spermBuffer = cloner.cloneField(cloneable.spermBuffer, EvaluatedGenomeStorage.class);
        this.ontology = cloneable.ontology;
        this.sensorRange = cloneable.sensorRange;
    }

    protected MatingReceiverAction(AbstractBuilder<?extends MatingReceiverAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.spermBuffer = builder.spermBuffer;
        this.ontology = builder.ontology;
        this.sensorRange = builder.sensorRange;
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<MatingReceiverAction, Builder> {
        @Override protected Builder self() { return this; }

        @Override
        protected MatingReceiverAction checkedBuild() {
            return new MatingReceiverAction(this);
        }
    }

    protected static abstract class AbstractBuilder<E extends MatingReceiverAction, T extends AbstractBuilder<E,T>> extends ContractNetParticipantAction.AbstractBuilder<E,T> {
        protected EvaluatedGenomeStorage spermBuffer = null;
        protected String ontology = "";
        protected double sensorRange = 1.0;

        public T spermStorage(EvaluatedGenomeStorage spermBuffer) { this.spermBuffer = checkNotNull(spermBuffer); return self(); }
        public T classification(String ontology) { this.ontology = checkNotNull(ontology); return self(); }
        public T searchRadius(double sensorRange) { this.sensorRange = sensorRange; return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(spermBuffer != null, "Builder must define a valid spermBuffer.");
            if (sensorRange <= 0)
                LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": sensorRange is <= 0 '" + sensorRange + "'");
            if (Strings.isNullOrEmpty(ontology))
                LOGGER.warn(MatingReceiverAction.class.getSimpleName() + ": ontology is invalid '" + ontology + "'");
        }
    }
}
