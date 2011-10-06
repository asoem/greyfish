package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloner;
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
    protected State executeUnconditioned(Simulation simulation) {
        if (nOffspring == 0 || spermStorage.isEmpty())
            return State.END_FAILED;

        LOGGER.debug("Producing {} offspring", nOffspring);

        for (int i = 0; i < nOffspring; i++) {

            simulation.createAgent(
                    agent.get().getPopulation(),
                    agent.get().getAnchorPoint(),
                    agent.get().createGamete()/*.mutated().recombined(spermStorage.getRWS())*/
            );
        }

        agent.get().getLog().add("offspring", nOffspring);
        return State.END_SUCCESS;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new ValueAdaptor<Integer>("Number of offspring", Integer.class) {
            @Override
            protected void set(Integer arg0) {
                nOffspring = checkNotNull(arg0);
            }

            @Override
            public Integer get() {
                return nOffspring;
            }
        });

        e.add(new FiniteSetValueAdaptor<EvaluatedGenomeStorage>("ImmutableGenome storage", EvaluatedGenomeStorage.class) {
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
                return Iterables.filter(agent.get().getProperties(), EvaluatedGenomeStorage.class);
            }
        });
    }

    @Override
    public SexualReproductionAction deepClone(DeepCloner cloner) {
        return new SexualReproductionAction(this, cloner);
    }

    private SexualReproductionAction(SexualReproductionAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.spermStorage = map.continueWith(cloneable.spermStorage, EvaluatedGenomeStorage.class);
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
        agent.get().getLog().set("offspring", 0);
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
