package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.ImmutableGenome;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.EvaluatedCandidate;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategy;
import org.asoem.greyfish.utils.collect.RandomSelectionStrategy;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.collect.ElementSelectionStrategies.pickAndPutBack;

@ClassGroup(tags="actions")
public class SexualReproductionAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproductionAction.class);
    
    @Element(name="property")
    private EvaluatedGenomeStorage spermStorage;

    @Attribute(name = "reproductive_value")
    private int clutch_size = 1;
    
    @Element
    private ElementSelectionStrategy<EvaluatedCandidate<Genome>> spermSelectionStrategy = new RandomSelectionStrategy<EvaluatedCandidate<Genome>>();

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

        for (EvaluatedCandidate<Genome> spermCandidate : pickAndPutBack(spermStorage.get(), spermSelectionStrategy, clutch_size)) {
            simulation.insertAgent(
                    agent().getPopulation(),
                    ImmutableGenome.mutatedCopyOf(ImmutableGenome.recombined(agent().getGenes(), spermCandidate.getObject())),
                    simulation.getSpace().getCoordinates(agent()));
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

        e.add("", new SetAdaptor<EvaluatedGenomeStorage>(EvaluatedGenomeStorage.class) {
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
