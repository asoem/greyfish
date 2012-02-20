package org.asoem.greyfish.core.actions;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.genes.EvaluatedGenome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.ImmutableGenome;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.io.AgentEvent;
import org.asoem.greyfish.core.io.AgentEventLogger;
import org.asoem.greyfish.core.io.AgentEventLoggerFactory;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategies;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategy;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.space.Coordinates2D;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="actions")
public class SexualReproductionAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproductionAction.class);

    private static final AgentEventLogger AGENT_EVENT_LOGGER = AgentEventLoggerFactory.getLogger();

    @Element(name="property")
    private EvaluatedGenomeStorage spermStorage;

    @Attribute(name = "reproductive_value")
    private int clutch_size = 1;
    
    @Element
    private ElementSelectionStrategy<EvaluatedGenome<?>> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();

    private int offspringCount = 0;

    @SimpleXMLConstructor
    public SexualReproductionAction() {
        this(new Builder());
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        if (clutch_size == 0 || spermStorage.isEmpty())
            return ActionState.END_FAILED;

        LOGGER.debug("Producing {} offspring ", clutch_size);

        final Population population = agent().getPopulation();
        final Coordinates2D coordinates = simulation.getSpace().getCoordinates(agent());

        for (EvaluatedGenome<?> spermCandidate : spermSelectionStrategy.pick(spermStorage.get(), clutch_size)) {
            final ImmutableGenome<Gene<?>> gamete = ImmutableGenome.mutatedCopyOf(ImmutableGenome.recombined(agent().getGenes(), spermCandidate));

            simulation.createAgent(population, gamete, coordinates);

            AGENT_EVENT_LOGGER.addEvent(new AgentEvent(simulation, simulation.getSteps(), agent(), this, "offspringProduced", "", coordinates));
        }

        offspringCount += clutch_size;

        return ActionState.END_SUCCESS;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Number of offspring", new AbstractTypedValueModel<Integer>() {
            @Override
            protected void set(Integer arg0) {
                clutch_size = checkNotNull(arg0);
            }

            @Override
            public Integer get() {
                return clutch_size;
            }
        });

        e.add("SpermPool", new SetAdaptor<EvaluatedGenomeStorage>(EvaluatedGenomeStorage.class) {
            @Override
            protected void set(EvaluatedGenomeStorage arg0) {
                spermStorage = checkNotNull(arg0);
            }

            @Override
            public EvaluatedGenomeStorage get() {
                return spermStorage;
            }

            @Override
            public Iterable<EvaluatedGenomeStorage> values() {
                return Iterables.filter(agent().getProperties(), EvaluatedGenomeStorage.class);
            }
        });
        
        e.add("Sperm selection strategy", new SetAdaptor<String>(String.class) {

            private final BiMap<String, ElementSelectionStrategy<EvaluatedGenome<?>>> strategies =
                    ImmutableBiMap.of(
                            "Random", ElementSelectionStrategies.<EvaluatedGenome<?>>randomSelection(),
                            "Roulette Wheel", ElementSelectionStrategies.<EvaluatedGenome<?>>rouletteWheelSelection(),
                            "Best", ElementSelectionStrategies.<EvaluatedGenome<?>>bestSelection());
            
            @Override
            public Iterable<String> values() {
                return strategies.keySet();
            }

            @Override
            protected void set(String arg0) {
                spermSelectionStrategy = strategies.get(arg0);
            }

            @Override
            public String get() {
                return strategies.inverse().get(spermSelectionStrategy);
            }
        });
    }

    @Override
    public SexualReproductionAction deepClone(DeepCloner cloner) {
        return new SexualReproductionAction(this, cloner);
    }

    private SexualReproductionAction(SexualReproductionAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.spermStorage = map.cloneField(cloneable.spermStorage, EvaluatedGenomeStorage.class);
        this.clutch_size = cloneable.clutch_size;
    }

    protected SexualReproductionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.spermStorage = builder.spermStorage;
        this.clutch_size = builder.nOffspring;
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        offspringCount = 0;
    }

    public static Builder with() { return new Builder(); }

    public int getOffspringCount() {
        return offspringCount;
    }

    public static final class Builder extends AbstractBuilder<SexualReproductionAction, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public SexualReproductionAction checkedBuild() { return new SexualReproductionAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends SexualReproductionAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private EvaluatedGenomeStorage spermStorage;
        private int nOffspring = 1;

        public T spermStorage(EvaluatedGenomeStorage spermStorage) { this.spermStorage = checkNotNull(spermStorage); return self(); }
        public T constantOffspringNumber(int nOffspring) { this.nOffspring = nOffspring; return self(); }
    }
}
