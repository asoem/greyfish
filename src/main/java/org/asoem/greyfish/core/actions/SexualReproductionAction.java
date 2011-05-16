package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="actions")
public class SexualReproductionAction extends AbstractGFAction {

    @Element(name="property")
    private EvaluatedGenomeStorage spermStorage;

    @Attribute(name = "reproductive_value")
    private int nOffspring = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproductionAction.class);

    @SimpleXMLConstructor
    public SexualReproductionAction() {
        this(new Builder());
    }

    @Override
    public boolean evaluateInternalState(Simulation simulation) {
        return nOffspring > 0 && !spermStorage.isEmpty();
    }

    @Override
    protected void performAction(Simulation simulation) {
        assert(!spermStorage.isEmpty());

        LOGGER.debug("Producing {} offspring", nOffspring);

        for (int i = 0; i < nOffspring; i++) {
            simulation.createAgent(
                    getComponentOwner().getPopulation(),
                    getComponentOwner().getAnchorPoint(),
                    getComponentOwner().getGenome().mutated().recombined(spermStorage.getRWS())
            );
        }

        getComponentOwner().getLog().add("offspring", nOffspring);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.add(new ValueAdaptor<Integer>("Number of offspring", Integer.class) {
            @Override
            protected void set(Integer arg0) {
                nOffspring = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public Integer get() {
                return nOffspring;
            }
        });

        e.add(new FiniteSetValueAdaptor<EvaluatedGenomeStorage>("Genome storage", EvaluatedGenomeStorage.class) {
            @Override
            protected void set(EvaluatedGenomeStorage arg0) {
                spermStorage = checkFrozen(checkNotNull(arg0));
            }

            @Override
            public EvaluatedGenomeStorage get() {
                return spermStorage;
            }

            @Override
            public Iterable<EvaluatedGenomeStorage> values() {
                return Iterables.filter(getComponentOwner().getProperties(), EvaluatedGenomeStorage.class);
            }
        });
    }

    @Override
    public SexualReproductionAction deepCloneHelper(CloneMap cloneMap) {
        return new SexualReproductionAction(this, cloneMap);
    }

    private SexualReproductionAction(SexualReproductionAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.spermStorage = map.clone(cloneable.spermStorage, EvaluatedGenomeStorage.class);
        this.nOffspring = cloneable.nOffspring;
    }

    protected SexualReproductionAction(AbstractBuilder<?> builder) {
        super(builder);
        this.spermStorage = builder.spermStorage;
        this.nOffspring = builder.nOffspring;
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        getComponentOwner().getLog().set("offspring", 0);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<SexualReproductionAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public SexualReproductionAction build() { return new SexualReproductionAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private EvaluatedGenomeStorage spermStorage;
        private int nOffspring = 1;

        public T spermStorage(EvaluatedGenomeStorage spermStorage) { this.spermStorage = checkNotNull(spermStorage); return self(); }
        public T constantOffspringNumber(int nOffspring) { this.nOffspring = nOffspring; return self(); }
    }
}
