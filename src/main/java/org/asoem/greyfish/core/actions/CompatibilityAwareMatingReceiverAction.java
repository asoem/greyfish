package org.asoem.greyfish.core.actions;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genes;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static org.asoem.greyfish.utils.RandomUtils.trueWithProbability;

/**
 * User: christoph
 * Date: 22.02.11
 * Time: 12:08
 */
@ClassGroup(tags="actions")
public class CompatibilityAwareMatingReceiverAction extends ContractNetInitiatorAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompatibilityAwareMatingReceiverAction.class);

    @Element(name="property")
    private EvaluatedGenomeStorage spermBuffer;

    @Element(name="compatibilityDefiningProperty")
    private GFProperty compatibilityDefiningProperty;

    @Element(name="messageType", required=false)
    private String ontology;

    @Element(name="sensorRange", required=false)
    private double sensorRange;

    private Iterable<Agent> sensedMates;

    @SuppressWarnings("unused")
    private CompatibilityAwareMatingReceiverAction() {
        this(new Builder());
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
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
                return filter(getComponentOwner().getProperties(), EvaluatedGenomeStorage.class);
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
        e.add(new FiniteSetValueAdaptor<GFProperty>("Compatibility Defining Property", GFProperty.class) {
            @Override
            protected void set(GFProperty arg0) {
                compatibilityDefiningProperty = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public GFProperty get() {
                return compatibilityDefiningProperty;
            }

            @Override
            public Iterable<GFProperty> values() {
                return getComponentOwner().getProperties();
            }
        });
    }

    private boolean receiveGenome(EvaluatedGenome genome) {
        if (spermBuffer != null) {
            spermBuffer.addGenome(genome, genome.getFitness());
            LOGGER.trace("{} received sperm: {}", getComponentOwner(), genome);
            return true;
        }
        return false;
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        checkNotNull(spermBuffer);
        checkNotNull(ontology);
    }

    @Override
    protected ACLMessage.Builder createCFP() {
        Agent.class.cast(getComponentOwner()).getLog().add("nMatingAttempts", 1);

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
            final EvaluatedGenome evaluatedGenome = message.getReferenceContent(EvaluatedGenome.class);

            double matingProbability = 0;
            if (compatibilityDefiningProperty != null) {
                final Iterable<Gene<?>> thisGenes = compatibilityDefiningProperty.getGenes();
                final Iterable<Gene<?>> thatGenes = evaluatedGenome.findCopiesFor(thisGenes);
                matingProbability = 1 - Genes.normalizedDistance(thisGenes, thatGenes);
            }

            if (trueWithProbability(matingProbability)) {
                LOGGER.debug("Accepting mating proposal with p={}", matingProbability);
                Agent.class.cast(getComponentOwner()).getLog().add("nMatingsAccepted", 1);

                receiveGenome(evaluatedGenome);
                builder.performative(ACLPerformative.ACCEPT_PROPOSAL);
            }
            else {
                LOGGER.debug("Refusing mating proposal with p={}", matingProbability);
                Agent.class.cast(getComponentOwner()).getLog().add("nMatingsRefused", 1);

                builder.performative(ACLPerformative.REJECT_PROPOSAL);
            }
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
    protected boolean canInitiate(ActionContext context) {
        final Iterable neighbours = context.findNeighbours(sensorRange);
        sensedMates = filter(neighbours, Agent.class);
        sensedMates = filter(sensedMates, not(equalTo(context.getAgent())));
        LOGGER.debug("Found {} possible mate(s)", Iterables.size(sensedMates));
        return ! Iterables.isEmpty(sensedMates);
    }

    @Override
    public CompatibilityAwareMatingReceiverAction deepCloneHelper(CloneMap cloneMap) {
        return new CompatibilityAwareMatingReceiverAction(this, cloneMap);
    }

    private CompatibilityAwareMatingReceiverAction(CompatibilityAwareMatingReceiverAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        this.spermBuffer = cloneMap.clone(cloneable.spermBuffer, EvaluatedGenomeStorage.class);
        this.ontology = cloneable.ontology;
        this.sensorRange = cloneable.sensorRange;
        this.compatibilityDefiningProperty = cloneMap.clone(cloneable.compatibilityDefiningProperty, GFProperty.class);

    }

    protected CompatibilityAwareMatingReceiverAction(AbstractBuilder<?> builder) {
        super(builder);
        this.spermBuffer = builder.spermBuffer;
        this.ontology = builder.ontology;
        this.sensorRange = builder.sensorRange;
        this.compatibilityDefiningProperty = builder.compatibilityDefiningProperty;
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        Agent.class.cast(getComponentOwner()).getLog().set("nMatingsAccepted", 0);
        Agent.class.cast(getComponentOwner()).getLog().set("nMatingsRefused", 0);
        Agent.class.cast(getComponentOwner()).getLog().set("nMatingAttempts", 0);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<CompatibilityAwareMatingReceiverAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public CompatibilityAwareMatingReceiverAction build() {
            return new CompatibilityAwareMatingReceiverAction(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends ContractNetParticipantAction.AbstractBuilder<T> {
        protected EvaluatedGenomeStorage spermBuffer = null;
        protected String ontology = "";
        protected double sensorRange = 1.0;
        protected GFProperty compatibilityDefiningProperty;

        public T compatibilityDefiningProperty(GFProperty property) { this.compatibilityDefiningProperty = checkNotNull(property); return self(); }
        public T storesSpermIn(EvaluatedGenomeStorage spermBuffer) { this.spermBuffer = checkNotNull(spermBuffer); return self(); }
        public T fromMatesOfType(String ontology) { this.ontology = checkNotNull(ontology); return self(); }
        public T closerThan(double sensorRange) { this.sensorRange = sensorRange; return self(); }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            if (sensorRange <= 0)
                LOGGER.warn("{}: sensorRange '{}' is <= 0.", this, sensorRange);
            if (Strings.isNullOrEmpty(ontology))
                LOGGER.warn("{}: ontology '{}' is invalid ", this, ontology);
        }
    }
}