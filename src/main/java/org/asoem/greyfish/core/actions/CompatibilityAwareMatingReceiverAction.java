package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genes;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.ImmutableMapBuilder;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static org.asoem.greyfish.utils.math.RandomUtils.trueWithProbability;

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
        e.add("Compatibility Defining Property", new SetAdaptor<GFProperty>(GFProperty.class) {
            @Override
            protected void set(GFProperty arg0) {
                compatibilityDefiningProperty = checkNotNull(arg0);
            }

            @Override
            public GFProperty get() {
                return compatibilityDefiningProperty;
            }

            @Override
            public Iterable<GFProperty> values() {
                return agent().getProperties();
            }
        });
    }

    private boolean receiveGenome(EvaluatedGenome genome) {
        if (spermBuffer != null) {
            spermBuffer.addGenome(genome, genome.getFitness());
            LOGGER.trace("{} received sperm: {}", getAgent(), genome);
            return true;
        }
        return false;
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> createCFP() {
        assert(!Iterables.isEmpty(sensedMates)); // see #evaluateCondition(Simulation)

        return ImmutableACLMessage.<Agent>with()
                .sender(getAgent())
                .performative(ACLPerformative.CFP)
                .ontology(ontology)
                        // Choose only one receiver. Adding all possible candidates as receivers will decrease the performance in high density populations!
                .addReceiver(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))));
    }

    @Override
    protected ImmutableACLMessage.Builder<Agent> handlePropose(ACLMessage<Agent> message) throws NotUnderstoodException {
        ImmutableACLMessage.Builder<Agent> builder = ImmutableACLMessage.createReply(message, this.getAgent());
        try {
            final EvaluatedGenome<?> evaluatedGenome = message.getContent(EvaluatedGenome.class);

            double matingProbability = 0;
            if (compatibilityDefiningProperty != null) {

                /*
               ImmutableMap<Gene<?>, Gene<?>> map = ImmutableMapBuilder.<Gene<?>, Gene<?>>newInstance().putAll(
                        compatibilityDefiningProperty.getGenes(),
                        Functions.<Gene<?>>identity(),
                        new Function<Gene<?>, Gene<?>>() {
                            @Override
                            public Gene<?> apply(@Nullable final Gene<?> gene) {
                                return Iterables.find(evaluatedGenome, new Predicate<Gene<?>>() {
                                    @Override
                                    public boolean apply(@Nullable Gene<?> o) {
                                        return o.isMutatedCopy(gene);
                                    }
                                });
                            }
                        }).build();

                matingProbability = 1 - Genes.normalizedDistance(map.keySet(), map.values());
                */
                matingProbability = 1; // todo: needs a different implementation, as properties don't have genes anymore
            }

            if (trueWithProbability(matingProbability)) {
                LOGGER.debug("Accepting mating proposal with p={}", matingProbability);

                receiveGenome(evaluatedGenome);
                builder.performative(ACLPerformative.ACCEPT_PROPOSAL);
            }
            else {
                LOGGER.debug("Refusing mating proposal with p={}", matingProbability);

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
    protected boolean canInitiate(Simulation simulation) {
        sensedMates = simulation.findNeighbours(agent(), sensorRange);
        LOGGER.debug("Found {} possible mate(s)", Iterables.size(sensedMates));
        return ! Iterables.isEmpty(sensedMates);
    }

    @Override
    public CompatibilityAwareMatingReceiverAction deepClone(DeepCloner cloner) {
        return new CompatibilityAwareMatingReceiverAction(this, cloner);
    }

    private CompatibilityAwareMatingReceiverAction(CompatibilityAwareMatingReceiverAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.spermBuffer = cloner.cloneField(cloneable.spermBuffer, EvaluatedGenomeStorage.class);
        this.ontology = cloneable.ontology;
        this.sensorRange = cloneable.sensorRange;
        this.compatibilityDefiningProperty = cloner.cloneField(cloneable.compatibilityDefiningProperty, GFProperty.class);

    }

    protected CompatibilityAwareMatingReceiverAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.spermBuffer = builder.spermBuffer;
        this.ontology = builder.ontology;
        this.sensorRange = builder.sensorRange;
        this.compatibilityDefiningProperty = builder.compatibilityDefiningProperty;
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<CompatibilityAwareMatingReceiverAction, Builder> {
        @Override protected Builder self() { return this; }
        @Override public CompatibilityAwareMatingReceiverAction checkedBuild() {
            return new CompatibilityAwareMatingReceiverAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends CompatibilityAwareMatingReceiverAction, T extends AbstractBuilder<E,T>> extends ContractNetParticipantAction.AbstractBuilder<E,T> {
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